package net.imglib2.atlas;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.actions.DeserializeClassifier;
import net.imglib2.atlas.actions.SerializeClassifier;
import net.imglib2.atlas.actions.ToggleVisibility;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.ClassifyingCellLoader;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.UpdatePrediction;
import net.imglib2.atlas.color.ColorMapColorProvider;
import net.imglib2.atlas.color.IntegerARGBConverters;
import net.imglib2.atlas.color.UpdateColormap;
import net.imglib2.atlas.control.brush.*;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converters;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.AbstractVolatileRealType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MainFrame {
	@SuppressWarnings( { "rawtypes" } )
	public static < R extends RealType< R >, F extends RealType< F > >
	BdvStackSource< ARGBType > trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final List<? extends RandomAccessibleInterval<F>> features,
			final Classifier<Composite<F>, RandomAccessibleInterval<F>, RandomAccessibleInterval<ShortType>> classifier,
			final int nLabels,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		final int numFetcherThreads = Runtime.getRuntime().availableProcessors();
		final SharedQueue queue = new SharedQueue( numFetcherThreads );

		final int nDim = rawData.numDimensions();
		final RandomAccessibleInterval< F > featuresConcatenated = Views.concatenate( nDim, features.stream().map(f -> f.numDimensions() == nDim ? Views.addDimension( f, 0, 0 ) : f ).collect( Collectors.toList() ) );

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

		final Random rng = new Random();
		final ColorMapColorProvider colorProvider = new ColorMapColorProvider( rng, LabelBrushController.BACKGROUND, 0 );

		// add labels layer
		System.out.println( "Adding labels layer" );
		final DiskCachedCellImgOptions labelsOpt = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( true );
		final DiskCachedCellImgFactory< IntType > labelsFac = new DiskCachedCellImgFactory<>( labelsOpt );
		CellLoader<IntType> loader = target -> target.forEach(x -> x.set(LabelBrushController.BACKGROUND));
		final DiskCachedCellImg< IntType, ? > labels = labelsFac.create( grid.getImgDimensions(), new IntType(), loader);
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
		final UpdatePrediction.CacheOptions cacheOptions = new UpdatePrediction.CacheOptions( "prediction", grid, queue );
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

		// add original
		BdvFunctions.show(rawData, "original", BdvOptions.options().addTo( bdv ));

		// add features
		System.out.println( "Adding features" );

		for ( int feat = 0; feat < features.size(); ++feat )
		{
			final BdvStackSource source = tryShowVolatile( features.get( feat ), "feature " + ( feat + 1 ), BdvOptions.options().addTo( bdv ), queue );
			source.setDisplayRange( 0, 255 );
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

	public static < T extends RealType< T >, V extends AbstractVolatileRealType< T, V >> BdvStackSource< ? > tryShowVolatile(
			final RandomAccessibleInterval< T > rai,
			final String name,
			final BdvOptions opts,
			final SharedQueue queue )
	{
		try
		{
			return BdvFunctions.show( VolatileViews.<T, V>wrapAsVolatile( rai, queue ), name, opts );
		}
		catch ( final IllegalArgumentException e )
		{
			return BdvFunctions.show( rai, name, opts );
		}
	}
}
