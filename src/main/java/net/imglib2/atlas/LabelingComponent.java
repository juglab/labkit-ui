package net.imglib2.atlas;

import bdv.util.*;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.DisplayMode;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
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
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
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
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class LabelingComponent<F extends RealType< F >> {

	private Classifier classifier;

	private SharedQueue queue = initQueue();

	private BdvHandle bdvHandle;

	private InputTriggerConfig config = new InputTriggerConfig();

	private Actions actions = new Actions( config );

	private Behaviours behaviors = new Behaviours(config);

	private MenuBar menu = new MenuBar();

	private JFrame frame = initFrame();

	@SuppressWarnings( { "rawtypes" } )
	public < R extends RealType< R > >
	BdvHandle trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final List<? extends RandomAccessibleInterval<F>> features,
			final Classifier classifier,
			final int nLabels,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		final Interval interval = new FinalInterval(rawData);
		this.classifier = classifier;

		final int nDim = rawData.numDimensions();

		final ColorMapColorProvider colorProvider = new ColorMapColorProvider(LabelBrushController.BACKGROUND, 0 );

		initBdv(isTimeSeries && nDim == 3);

		final LabelBrushController brushController = initLabelsLayer(nLabels, grid, isTimeSeries, colorProvider);

		final RandomAccessibleInterval<F> featuresConcatenated = concatenateFeatures(features, nDim);
		final TrainClassifier<F> trainer = initTrainer(nLabels, grid, interval, colorProvider, brushController, featuresConcatenated);
		addAction(trainer, "ctrl shift T");
		addAction(new ToggleVisibility( "Toggle Classification", bdvHandle.getViewerPanel(), 1 ), "C");

		BdvFunctions.show(rawData, "original", BdvOptions.options().addTo( bdvHandle ));
		bdvAddFeatures(bdvHandle, features);

		final int nFeatures = ( int ) featuresConcatenated.dimension( nDim );
		initMouseWheelSelection(nFeatures);
		initSaveClassifierAction();
		initLoadClassifierAction(trainer);

		behaviors.install( bdvHandle.getTriggerbindings(), "classifier training" );
		actions.install( bdvHandle.getKeybindings(), "classifier training" );
		frame.setVisible(true);
		return bdvHandle;
	}

	private SharedQueue initQueue() {
		final int numFetcherThreads = Runtime.getRuntime().availableProcessors();
		return new SharedQueue( numFetcherThreads );
	}

	private RandomAccessibleInterval<F> concatenateFeatures(List<? extends RandomAccessibleInterval<F>> features, int nDim) {
		return Views.concatenate( nDim, features.stream().map(f -> f.numDimensions() == nDim ? Views.addDimension( f, 0, 0 ) : f ).collect( Collectors.toList() ) );
	}

	private static int[] cellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[ grid.numDimensions() ];
		grid.cellDimensions( cellDimensions );
		return cellDimensions;
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("ATLAS");
		frame.setJMenuBar(menu);
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	private void addAction(AbstractNamedAction action, String keyStroke) {
		JMenuItem item = new JMenuItem(action);
		menu.add(action);
		actions.namedAction(action, keyStroke);
	}


	private void initBdv(boolean is2D) {
		final BdvOptions options = BdvOptions.options();
		if (is2D)
			options.is2D();
		bdvHandle = new BdvHandlePanel(frame, options);
		frame.add(bdvHandle.getViewerPanel());
		bdvHandle.getViewerPanel().setDisplayMode( DisplayMode.FUSED );
	}

	private PaintPixelsGenerator<IntType, ? extends Iterator<IntType>> initPixelGenerator(boolean isTimeSeries, int numDimensions) {
		if ( isTimeSeries )
			return new NeighborhoodPixelsGeneratorForTimeSeries<>(numDimensions - 1, new NeighborhoodPixelsGenerator<IntType>(NeighborhoodFactories.hyperSphere(), 1.0));
		else
			return new NeighborhoodPixelsGenerator<>( NeighborhoodFactories.< IntType >hyperSphere(), 1.0 );
	}

	private Img<IntType> initCachedLabelsImg(CellGrid grid, int[] cellDimensions) {
		final DiskCachedCellImgOptions labelsOpt = DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ).dirtyAccesses( true );
		final DiskCachedCellImgFactory< IntType > labelsFac = new DiskCachedCellImgFactory<>( labelsOpt );
		CellLoader<IntType> loader = target -> target.forEach(x -> x.set(LabelBrushController.BACKGROUND));
		return labelsFac.create( grid.getImgDimensions(), new IntType(), loader);
	}

	private LabelBrushController initLabelsLayer(int nLabels, CellGrid grid, boolean isTimeSeries, ColorMapColorProvider colorProvider) {
		final int[] cellDimensions = cellDimensions(grid);
		final Img<IntType> labels = initCachedLabelsImg(grid, cellDimensions);
		BdvFunctions.show( Converters.convert( (RandomAccessibleInterval< IntType >) labels, new IntegerARGBConverters.ARGB<>( colorProvider ), new ARGBType() ), "labels", BdvOptions.options().addTo(bdvHandle) );
		final LabelBrushController brushController = new LabelBrushController(
				bdvHandle.getViewerPanel(),
				labels,
				initPixelGenerator(isTimeSeries, labels.numDimensions()),
				behaviors,
				nLabels,
				LabelBrushController.emptyGroundTruth(),
				colorProvider );
		initColorMapUpdaterAction(nLabels, colorProvider);
		addAction(new ToggleVisibility( "Toggle Labels", bdvHandle.getViewerPanel(), 0 ), "L");
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
		return brushController;
	}

	private void initColorMapUpdaterAction(int nLabels, ColorMapColorProvider colorProvider) {
		final UpdateColormap colormapUpdater = new UpdateColormap( colorProvider, nLabels, bdvHandle.getViewerPanel(), 1.0f );
		colormapUpdater.updateColormap();
		addAction(colormapUpdater, "ctrl shift C");
	}

	private RandomAccessibleContainer<VolatileARGBType> initPredictionLayer(Interval interval, int nDim) {
		// add prediction layer
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), nDim );
		final RandomAccessibleContainer< VolatileARGBType > container = new RandomAccessibleContainer<>( emptyPrediction );
		BdvFunctions.show( container, interval, "prediction", BdvOptions.options().addTo( bdvHandle ) );
		return container;
	}

	private TrainClassifier<F> initTrainer(int nLabels, CellGrid grid, Interval interval, ColorMapColorProvider colorProvider, LabelBrushController brushController, RandomAccessibleInterval<F> featuresConcatenated) {
		final RandomAccessibleContainer<VolatileARGBType> container = initPredictionLayer(interval, grid.numDimensions());
		final UpdatePrediction.CacheOptions cacheOptions = new UpdatePrediction.CacheOptions( "prediction", grid, queue);
		final ClassifyingCellLoader< F > classifyingLoader = new ClassifyingCellLoader<>(featuresConcatenated, this.classifier);
		final UpdatePrediction< F > predictionAdder = new UpdatePrediction<>(bdvHandle.getViewerPanel(), classifyingLoader, colorProvider, cacheOptions, container);
		final ArrayList< String > classes = new ArrayList<>();
		for (int i = 1; i <= nLabels; ++i )
			classes.add( "" + i );

		final TrainClassifier< F > trainer = new TrainClassifier<>(this.classifier, brushController, featuresConcatenated, classes );
		trainer.addListener( predictionAdder );
		return trainer;
	}

	private void bdvAddFeatures(BdvHandle bdv, List<? extends RandomAccessibleInterval<F>> features) {
		for ( int feat = 0; feat < features.size(); ++feat )
		{
			final BdvStackSource source = tryShowVolatile( features.get( feat ), "feature " + ( feat + 1 ), BdvOptions.options().addTo( bdv ) );
			source.setDisplayRange( 0, 255 );
			source.setActive( false );
		}
	}

	private void initMouseWheelSelection(int nFeatures) {
		final MouseWheelChannelSelector mouseWheelSelector = new MouseWheelChannelSelector(bdvHandle.getViewerPanel(), 2, nFeatures );
		behaviors.behaviour( mouseWheelSelector, "mouseweheel selector", "shift F scroll" );
		behaviors.behaviour( mouseWheelSelector.getOverlay(), "feature selector overlay", "shift F" );
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( mouseWheelSelector.getOverlay() );
	}

	private void initSaveClassifierAction() {
		final SerializeClassifier saveDialogAction = new SerializeClassifier( "classifier-serializer", bdvHandle.getViewerPanel(), this.classifier);
		addAction(saveDialogAction, "ctrl S");
	}

	private void initLoadClassifierAction(TrainClassifier<F> trainer) {
		final DeserializeClassifier loadDialogAction = new DeserializeClassifier(bdvHandle.getViewerPanel(), this.classifier, trainer.getListeners() );
		addAction(loadDialogAction, "ctrl O");
	}

	public < T extends RealType< T >, V extends AbstractVolatileRealType< T, V >> BdvStackSource< ? > tryShowVolatile(
			final RandomAccessibleInterval< T > rai,
			final String name,
			final BdvOptions opts)
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
