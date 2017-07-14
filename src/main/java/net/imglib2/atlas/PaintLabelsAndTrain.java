package net.imglib2.atlas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;
import hr.irb.fastRandomForest.FastRandomForest;
import ij.ImagePlus;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.ClassifyingCellLoader;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.UpdatePrediction;
import net.imglib2.atlas.classification.UpdatePrediction.CacheOptions;
import net.imglib2.atlas.classification.weka.WekaClassifier;
import net.imglib2.atlas.color.ColorMapColorProvider;
import net.imglib2.atlas.color.IntegerARGBConverters;
import net.imglib2.atlas.color.UpdateColormap;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.atlas.control.brush.NeighborhoodFactories;
import net.imglib2.atlas.control.brush.NeighborhoodPixelsGenerator;
import net.imglib2.atlas.control.brush.NeighborhoodPixelsGeneratorForTimeSeries;
import net.imglib2.atlas.control.brush.PaintPixelsGenerator;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.AbstractVolatileRealType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

public class PaintLabelsAndTrain
{

	public static void main( final String[] args ) throws IncompatibleTypeException, IOException
	{

		final Random rng = new Random();
		final String imgPath = System.getProperty( "user.home" ) + "/Downloads/epfl-em/training.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );
		final int[] cellDimensions = new int[] { 128, 128, 2 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );
		final int numFetcherThreads = Runtime.getRuntime().availableProcessors();
		final SharedQueue queue = new SharedQueue( numFetcherThreads );


		final int nLabels = 2;
		final List< String > classLabels = IntStream.range( 0, nLabels ).mapToObj( l -> "class " + l ).collect( Collectors.toList() );

		final ArrayImg< UnsignedByteType, ByteArray > rawData = ArrayImgs.unsignedBytes( dimensions );
		for ( final Pair< UnsignedByteType, UnsignedByteType > p : Views.interval( Views.pair( rawImg, rawData ), rawImg ) )
			p.getB().set( p.getA() );
		final RandomAccessibleInterval< FloatType > converted = Converters.convert( ( RandomAccessibleInterval< UnsignedByteType > ) rawData, new RealFloatConverter<>(), new FloatType() );

		final ArrayList<RandomAccessibleInterval<FloatType>> featuresList = initFeatures(grid, converted);

		final RandomAccessibleInterval< FloatType > features = Views.concatenate( 3, featuresList );

		final FastRandomForest wekaClassifier = new FastRandomForest();
		final WekaClassifier< FloatType, ShortType > classifier = new WekaClassifier<>( wekaClassifier, classLabels, ( int ) features.dimension( features.numDimensions() - 1 ) );

		trainClassifier( rawData, featuresList, classifier, nLabels, grid, queue, true, rng );
	}

	private static ArrayList<RandomAccessibleInterval<FloatType>> initFeatures(CellGrid grid, RandomAccessibleInterval<FloatType> original) {
		long[] dimensions = Intervals.dimensionsAsLongArray(original);
		int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		final ArrayList< RandomAccessibleInterval< FloatType > > featuresList = new ArrayList<>();
		featuresList.add( Views.addDimension(original, 0, 0 ) );
		final double[] sigmas = { 1.0 }; // , 3.0, 5.0, 7.0 };
		@SuppressWarnings( "unchecked" )
		final DiskCachedCellImg< FloatType, ? >[] gausses = new DiskCachedCellImg[ sigmas.length ];
		final DiskCachedCellImgOptions featureOpts = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( false );
		final DiskCachedCellImgFactory< FloatType > featureFactory = new DiskCachedCellImgFactory<>( featureOpts );
		for ( int sigmaIndex = 0; sigmaIndex < sigmas.length; ++sigmaIndex )
		{
			final double sigma = sigmas[ sigmaIndex ];
			final double sigmaDiff = sigmaIndex == 0 ? sigma : Math.sqrt( sigma * sigma - sigmas[ sigmaIndex - 1 ] * sigmas[ sigmaIndex - 1 ] );
//			final ArrayImg< FloatType, FloatArray > gauss = ArrayImgs.floats( Intervals.dimensionsAsLongArray( original ) );
//			Gauss3.gauss( sigma, Views.extendBorder( converted ), gauss );
			final RandomAccessibleInterval< FloatType > gaussSource = sigmaIndex == 0 ? original : gausses[ sigmaIndex - 1 ];
			final FeatureGeneratorLoader< FloatType, FloatType > gaussLoader = new FeatureGeneratorLoader<>( grid, target -> {
				Gauss3.gauss( sigmaDiff, Views.extendBorder( gaussSource ), target );
			} );
			final DiskCachedCellImg< FloatType, ? > gauss = featureFactory.create( dimensions, new FloatType(), gaussLoader );
			gausses[ sigmaIndex ] = gauss;
			featuresList.add( Views.addDimension( gauss, 0, 0 ) );

			@SuppressWarnings( "unchecked" )
			final Img< FloatType >[] gradients = new Img[ original.numDimensions() ];
			for (int d = 0; d < original.numDimensions(); ++d )
			{
				final int finalD = d;
				final FeatureGeneratorLoader< FloatType, FloatType > gradientLoader = new FeatureGeneratorLoader<>( grid, target -> {
					PartialDerivative.gradientCentralDifference2( Views.extendBorder( gauss ), target, finalD );
				} );
				final DiskCachedCellImg< FloatType, ? > grad = featureFactory.create( dimensions, new FloatType(), gradientLoader );
				gradients[ d ] = grad;
			}

			final FeatureGeneratorLoader< FloatType, FloatType > gradientMagnitudeLoader = new FeatureGeneratorLoader<>( grid, target -> {
				final FloatType ft = new FloatType();
				for ( int d = 0; d < gradients.length; ++d )
					for ( final Pair< FloatType, FloatType > p : Views.interval( Views.pair( gradients[ d ], target ), target ) )
					{
						final float v = p.getA().get();
						ft.set( v * v );
						p.getB().add( ft );
					}
			} );

			final DiskCachedCellImg< FloatType, ? > gradientMagnitude = featureFactory.create( dimensions, new FloatType(), gradientMagnitudeLoader );

			featuresList.add( Views.addDimension( gradientMagnitude, 0, 0 ) );
		}
		return featuresList;
	}

	public static < R extends RealType< R >, F extends RealType< F > >
	BdvStackSource< ARGBType > trainClassifier(
			final RandomAccessibleInterval< R > rawData,
			final List< ? extends RandomAccessibleInterval< F > > features,
					final Classifier< Composite< F >, RandomAccessibleInterval< F >, RandomAccessibleInterval< ShortType > > classifier,
					final int nLabels,
					final CellGrid grid,
					final SharedQueue queue,
					final boolean isTimeSeries ) throws IOException
	{
		return trainClassifier( rawData, features, classifier, nLabels, grid, queue, isTimeSeries, new Random( 100 ) );
	}

	@SuppressWarnings( { "rawtypes" } )
	public static < R extends RealType< R >, F extends RealType< F > >
	BdvStackSource< ARGBType > trainClassifier(
			final RandomAccessibleInterval< R > rawData,
			final List< ? extends RandomAccessibleInterval< F > > features,
					final Classifier< Composite< F >, RandomAccessibleInterval< F >, RandomAccessibleInterval< ShortType > > classifier,
					final int nLabels,
					final CellGrid grid,
					final SharedQueue queue,
					final boolean isTimeSeries,
					final Random rng ) throws IOException
	{

		final int nDim = rawData.numDimensions();
		final RandomAccessibleInterval< F > featuresConcatenated = Views.concatenate( nDim, features.stream().map( f -> f.numDimensions() == nDim ? Views.addDimension( f, 0, 0 ) : f ).collect( Collectors.toList() ) );

		final int[] cellDimensions = new int[ grid.numDimensions() ];
		grid.cellDimensions( cellDimensions );
		System.out.println( "Entering train method" );
		final InputTriggerConfig config = new InputTriggerConfig();
		final Behaviours behaviors = new Behaviours( config );
		final Actions actions = new Actions( config );

		final BdvOptions options = BdvOptions.options();
		options.frameTitle( "ATLAS" );
		if ( isTimeSeries && rawData.numDimensions() == 3 )
			options.is2D();

		PaintPixelsGenerator< IntType, ? extends Iterator< IntType > > pixelGenerator;
		if ( isTimeSeries )
			pixelGenerator = new NeighborhoodPixelsGeneratorForTimeSeries<>( rawData.numDimensions() - 1, new NeighborhoodPixelsGenerator< IntType >( NeighborhoodFactories.hyperSphere(), 1.0 ) );
		else
			pixelGenerator = new NeighborhoodPixelsGenerator<>( NeighborhoodFactories.< IntType >hyperSphere(), 1.0 );

		final ColorMapColorProvider colorProvider = new ColorMapColorProvider( rng, LabelBrushController.BACKGROUND, 0 );

		// add labels layer
		System.out.println( "Adding labels layer" );
		final DiskCachedCellImgOptions labelsOpt = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( true );
		final DiskCachedCellImgFactory< IntType > labelsFac = new DiskCachedCellImgFactory<>( labelsOpt );
		final DiskCachedCellImg< IntType, ? > labels = labelsFac.create( grid.getImgDimensions(), new IntType(), new LabelLoader<>( grid, LabelBrushController.BACKGROUND ) );
		final BdvStackSource< ARGBType > bdv = BdvFunctions.show( Converters.convert( ( RandomAccessibleInterval< IntType > ) labels, new IntegerARGBConverters.ARGB<>( colorProvider ), new ARGBType() ), "labels", options );
		final ViewerPanel viewer = bdv.getBdvHandle().getViewerPanel();
		final AffineTransform3D labelTransform = new AffineTransform3D();
		final LabelBrushController brushController = new LabelBrushController(
				viewer,
				labels,
				pixelGenerator,
				labelTransform,
				behaviors,
				nLabels,
				LabelBrushController.emptyGroundTruth(),
				colorProvider );
		final UpdateColormap colormapUpdater = new UpdateColormap( colorProvider, nLabels, rng, viewer, 1.0f );
		colormapUpdater.updateColormap();
		actions.namedAction( new ToggleVisibility( "toggle labels", viewer, 0 ), "L" );

		// set up viewer
		System.out.println( "Setting up viewer" );
		viewer.getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
		viewer.setDisplayMode( DisplayMode.FUSED );

		// add prediction layer
		System.out.println( "Adding prediction layer" );
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), labels.numDimensions() );
		final RandomAccessibleContainer< VolatileARGBType > container = new RandomAccessibleContainer<>( emptyPrediction );
		final BdvStackSource< VolatileARGBType > bdvPrediction = BdvFunctions.show( container, labels, "prediction", BdvOptions.options().addTo( bdv ) );
		System.out.println( "bdvPrediction: " + bdvPrediction );

		final int nFeatures = ( int ) featuresConcatenated.dimension( nDim );
		final CacheOptions cacheOptions = new UpdatePrediction.CacheOptions( "prediction", grid, queue );
		final ClassifyingCellLoader< F > classifyingLoader = new ClassifyingCellLoader<>( featuresConcatenated, classifier, nFeatures );
		final UpdatePrediction< F > predictionAdder = new UpdatePrediction<>( viewer, classifyingLoader, colorProvider, cacheOptions, container );
		final ArrayList< String > classes = new ArrayList<>();
		for ( int i = 1; i <= nLabels; ++i )
			classes.add( "" + i );

		final TrainClassifier< F > trainer = new TrainClassifier<>( classifier, brushController, featuresConcatenated, classes );
		trainer.addListener( predictionAdder );
		actions.namedAction( trainer, "ctrl shift T" );
		actions.namedAction( colormapUpdater, "ctrl shift C" );
		actions.namedAction( new ToggleVisibility( "toggle classification", viewer, 1 ), "C" );


		// add features
		System.out.println( "Adding features" );

		final BdvStackSource featBdv = tryShowVolatile( features.get( 0 ), "feature 1", BdvOptions.options().addTo( bdv ), queue );
		System.out.println( "added first feature: " + bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().size() + " " + featBdv );
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 2 ).setRange( 0, 255 );
		for ( int feat = 1; feat < features.size(); ++feat )
		{
			final BdvStackSource source = tryShowVolatile( features.get( feat ), "feature " + ( feat + 1 ), BdvOptions.options().addTo( bdv ), queue );
//					BdvFunctions.show( VolatileViews.wrapAsVolatile( features.get( feat ) ), "feature " + ( feat + 1 ), BdvOptions.options().addTo( bdv ) );
			bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( feat + 2 ).setRange( 0, 255 );
			source.setActive( false );
		}

		final MouseWheelChannelSelector mouseWheelSelector = new MouseWheelChannelSelector( viewer, 2, nFeatures );
		behaviors.behaviour( mouseWheelSelector, "mouseweheel selector", "shift F scroll" );
		behaviors.behaviour( mouseWheelSelector.getOverlay(), "feature selector overlay", "shift F" );
		viewer.getDisplay().addOverlayRenderer( mouseWheelSelector.getOverlay() );

		final SerializeClassifier saveDialogAction = new SerializeClassifier( "classifier-serializer", viewer, classifier );
		actions.namedAction( saveDialogAction, "ctrl S" );

		final DeserializeClassifier loadDialogAction = new DeserializeClassifier( "classifier-deserializer", viewer, classifier, trainer.getListeners() );
		actions.namedAction( loadDialogAction, "ctrl O" );

		// install actions and behaviors
		System.out.println( "Installing actions and behaviors" );
		actions.install( bdv.getBdvHandle().getKeybindings(), "classifier training" );
		behaviors.install( bdv.getBdvHandle().getTriggerbindings(), "classifier training" );
		return bdv;
	}

	public static < T extends RealType< T >, V extends AbstractVolatileRealType< T, V > > BdvStackSource< ? > tryShowVolatile(
			final RandomAccessibleInterval< T > rai,
			final String name,
			final BdvOptions opts,
			final SharedQueue queue )
	{
		try
		{
			return BdvFunctions.show( VolatileViews.< T, V >wrapAsVolatile( rai, queue ), name, opts );
		}
		catch ( final IllegalArgumentException e )
		{
			return BdvFunctions.show( rai, name, opts );
		}
	}


}
