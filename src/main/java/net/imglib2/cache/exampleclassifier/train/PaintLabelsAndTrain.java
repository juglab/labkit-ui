package net.imglib2.cache.exampleclassifier.train;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;
import gnu.trove.map.hash.TIntIntHashMap;
import hr.irb.fastRandomForest.FastRandomForest;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.cache.exampleclassifier.train.AddClassifierToBdv.CacheOptions;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.queue.FetcherThreads;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DirtyIntArray;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.LazyCellImg;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import weka.classifiers.Classifier;

public class PaintLabelsAndTrain
{

	public static void main( final String[] args ) throws IncompatibleTypeException, IOException
	{

		final Random rng = new Random();
		final String imgPath = System.getProperty( "user.home" ) + "/Downloads/epfl-em/training.tif";
		final Img< UnsignedByteType > rawImg = ImageJFunctions.wrapByte( new ImagePlus( imgPath ) );
		final long[] dimensions = Intervals.dimensionsAsLongArray( rawImg );

		final int[] cellDimensions = new int[] { 32, 32, 4 };
		final CellGrid grid = new CellGrid( dimensions, cellDimensions );
		final int maxNumLevels = 1;
		final int numFetcherThreads = Runtime.getRuntime().availableProcessors();
		final BlockingFetchQueues< Callable< ? > > queue = new BlockingFetchQueues<>( maxNumLevels );
		new FetcherThreads( queue, numFetcherThreads );

		final int nLabels = 2;

		final ArrayImg< UnsignedByteType, ByteArray > rawData = ArrayImgs.unsignedBytes( dimensions );
		for ( final Pair< UnsignedByteType, UnsignedByteType > p : Views.interval( Views.pair( rawImg, rawData ), rawImg ) )
			p.getB().set( p.getA() );
		final FastRandomForest classifier = new FastRandomForest();
		final ArrayList< RandomAccessibleInterval< FloatType > > featuresList = new ArrayList<>();
		final ArrayList< RandomAccessibleInterval< VolatileFloatType > > vfeaturesList = new ArrayList<>();
		final RandomAccessibleInterval< FloatType > converted = Converters.convert( ( RandomAccessibleInterval< UnsignedByteType > ) rawData, new RealFloatConverter<>(), new FloatType() );
		featuresList.add( Views.addDimension( converted, 0, 0 ) );
		vfeaturesList.add( Converters.convert( featuresList.get( 0 ), ( Converter< FloatType, VolatileFloatType > ) ( input, output ) -> {
			output.setValid( true );
			output.set( input.get() );
		}, new VolatileFloatType() ) );
		final double[] sigmas = { 1.0 }; // , 3.0, 5.0, 7.0 };
		@SuppressWarnings( "unchecked" )
		final Pair< Img< FloatType >, Img< VolatileFloatType > >[] gausses = new Pair[ sigmas.length ];
		for ( int sigmaIndex = 0; sigmaIndex < sigmas.length; ++sigmaIndex )
		{
			final double sigma = sigmas[ sigmaIndex ];
			final double sigmaDiff = sigmaIndex == 0 ? sigma : Math.sqrt( sigma * sigma - sigmas[ sigmaIndex - 1 ] * sigmas[ sigmaIndex - 1 ] );
//			final ArrayImg< FloatType, FloatArray > gauss = ArrayImgs.floats( Intervals.dimensionsAsLongArray( converted ) );
//			Gauss3.gauss( sigma, Views.extendBorder( converted ), gauss );
			final RandomAccessibleInterval< FloatType > gaussSource = sigmaIndex == 0 ? converted : gausses[ sigmaIndex - 1 ].getA();
			final FeatureGeneratorLoader< FloatType > gaussLoader = new FeatureGeneratorLoader<>( grid, target -> {
				Gauss3.gauss( sigmaDiff, Views.extendBorder( gaussSource ), target );
			} );
			final Pair< Img< FloatType >, Img< VolatileFloatType > > gauss = FeatureGeneratorLoader.createFeatures( gaussLoader, "gauss-" + sigma + "-", 1000, queue );
			gausses[ sigmaIndex ] = gauss;
			featuresList.add( Views.addDimension( gauss.getA(), 0, 0 ) );
			vfeaturesList.add( Views.addDimension( gauss.getB(), 0, 0 ) );

			@SuppressWarnings( "unchecked" )
			final Pair< Img< FloatType >, Img< VolatileFloatType > >[] gradients = new Pair[ converted.numDimensions() ];
			for ( int d = 0; d < converted.numDimensions(); ++d )
			{
				final int finalD = d;
				final FeatureGeneratorLoader< FloatType > gradientLoader = new FeatureGeneratorLoader<>( grid, target -> {
					PartialDerivative.gradientCentralDifference2( Views.extendBorder( gauss.getA() ), target, finalD );
				} );
				final Pair< Img< FloatType >, Img< VolatileFloatType > > grad = FeatureGeneratorLoader.createFeatures( gradientLoader, "gradient-" + d + "-" + sigma + "-", 1000, queue );
				gradients[ d ] = grad;
			}

			final FeatureGeneratorLoader< FloatType > gradientMagnitudeLoader = new FeatureGeneratorLoader<>( grid, target -> {
				final FloatType ft = new FloatType();
				for ( int d = 0; d < gradients.length; ++d )
					for ( final Pair< FloatType, FloatType > p : Views.interval( Views.pair( gradients[ d ].getA(), target ), target ) )
					{
						final float v = p.getA().get();
						ft.set( v * v );
						p.getB().add( ft );
					}
			} );

			final Pair< Img< FloatType >, Img< VolatileFloatType > > gradientMagnitude = FeatureGeneratorLoader.createFeatures( gradientMagnitudeLoader, "gradient-magnitude-" + sigma + "-", 1000, queue );

			featuresList.add( Views.addDimension( gradientMagnitude.getA(), 0, 0 ) );
			vfeaturesList.add( Views.addDimension( gradientMagnitude.getB(), 0, 0 ) );
		}

		final RandomAccessibleInterval< FloatType > features = Views.concatenate( 3, featuresList );
		final RandomAccessibleInterval< VolatileFloatType > vfeatures = Views.concatenate( 3, vfeaturesList );


		trainClassifier( rawData, features, vfeatures, classifier, nLabels, grid, queue, rng );
	}

	public static < R extends RealType< R >, F extends RealType< F >, VF extends Volatile< F > >
	void trainClassifier(
			final RandomAccessibleInterval< R > rawData,
			final RandomAccessibleInterval< F > features,
			final RandomAccessibleInterval< VF > volatileFeatures,
			final Classifier classifier,
			final int nLabels,
			final CellGrid grid,
			final BlockingFetchQueues< Callable< ? > > queue,
			final Random rng ) throws IOException
	{

		final long nFeatures = features.dimension( features.numDimensions() - 1 );
		final TIntIntHashMap cmap = new TIntIntHashMap();
		cmap.put( 0, 0 );

		final Converter< IntType, ARGBType > conv = ( input, output ) -> {
			output.set( cmap.get( input.getInteger() ) );
		};

		final InputTriggerConfig config = new InputTriggerConfig();
		final Behaviours behaviors = new Behaviours( config );

		final MouseWheelSelectorRandomAccessibleInterval< VF > selectingVFeatures = new MouseWheelSelectorRandomAccessibleInterval<>( volatileFeatures, volatileFeatures.numDimensions() - 1 );

//		final BdvStackSource< ? extends RealType< ? > > bdv = BdvFunctions.show( rawData, "raw" );
		final BdvStackSource< VF > bdv = BdvFunctions.show( selectingVFeatures, "features" );
		bdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 255 );
		final ViewerPanel viewer = bdv.getBdvHandle().getViewerPanel();

		final MouseWheelSelector mouseWheelSelector = new MouseWheelSelector( selectingVFeatures, viewer );
		behaviors.behaviour( mouseWheelSelector, "mouseweheel selector", "shift F scroll" );

		final LazyCellImg< IntType, DirtyIntArray > labels = LabelLoader.createImg( new LabelLoader( grid, LabelBrushController.BACKGROUND ), "labels-", 1000 );
//		final ArrayImg< IntType, IntArray > labels = ArrayImgs.ints( Intervals.dimensionsAsLongArray( rawData ) );
		for ( final IntType l : labels )
			l.set( LabelBrushController.BACKGROUND );

		final LabelBrushController brushController = new LabelBrushController(
				viewer,
				labels,
				new AffineTransform3D(),
				behaviors,
				nLabels,
				LabelBrushController.emptyGroundTruth() );
		final UpdateColormap cmapUpdater = new UpdateColormap( cmap, nLabels, rng, viewer, 1.0f );
		cmapUpdater.updateColormap();
		bdv.getBdvHandle().getViewerPanel().setDisplayMode( DisplayMode.FUSED );
//		final SparseIntRandomAccessibleInterval< UnsignedShortType > labels = new SparseIntRandomAccessibleInterval<>( brushController.getGroundTruth(), rawData, new UnsignedShortType(), LabelBrushController.BACKGROUND );
		BdvFunctions.show( Converters.convert( ( RandomAccessibleInterval< IntType > ) labels, conv, new ARGBType() ), "labels", BdvOptions.options().addTo( bdv ) );
		behaviors.install( bdv.getBdvHandle().getTriggerbindings(), "paint ground truth" );
		bdv.getBdvHandle().getViewerPanel().getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
//		final BdvStackSource< VF > featuresBdv = BdvFunctions.show( volatileFeatures, "features" );
//		featuresBdv.getBdvHandle().getSetupAssignments().getMinMaxGroups().get( 0 ).setRange( 0, 255 );

		final Actions actions = new Actions( config );
		actions.install( bdv.getBdvHandle().getKeybindings(), "paint ground truth" );
		brushController.getBrushOverlay().setCmap( cmap );

		final ArrayList< String > classes = new ArrayList<>();
		for ( int i = 1; i <= nLabels; ++i )
			classes.add( "" + i );

		final TrainClassifier< F > trainer = new TrainClassifier<>( classifier, brushController, features, classes );
		actions.namedAction( trainer, "ctrl shift T" );
		actions.namedAction( cmapUpdater, "ctrl shift C" );

		final CacheOptions cacheOptions = new AddClassifierToBdv.CacheOptions( "prediction", grid, 1000, queue );
		final AddClassifierToBdv< F > predictionAdder = new AddClassifierToBdv<>( bdv, new ClassifyingCellLoader<>( grid, Views.collapseReal( features ), classifier, ( int ) nFeatures, nLabels ), cmap, cacheOptions );
		trainer.addListener( predictionAdder );

	}

}
