package net.imglib2.atlas;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import gnu.trove.map.hash.TLongIntHashMap;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.RevampUtils;
import net.imglib2.atlas.actions.DeserializeClassifier;
import net.imglib2.atlas.actions.SerializeClassifier;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.ClassifyingCellLoader;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.UpdatePrediction;
import net.imglib2.atlas.color.ColorMapColorProvider;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame< F extends RealType<F> > {

	private JFrame frame = initFrame();

	private Classifier classifier;

	private SharedQueue queue = initQueue();

	private BdvHandle bdvHandle;

	private LabelingComponent labelingComponent;

	private RandomAccessibleContainer<F> featureContainer;

	private List<RandomAccessibleInterval<F>> slices;

	public < R extends RealType< R > >
	void trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final List<? extends RandomAccessibleInterval<F>> features,
			final Classifier classifier,
			final int nLabels,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		this.classifier = classifier;

		labelingComponent = new LabelingComponent(frame);

		bdvHandle = labelingComponent.trainClassifier(rawData, nLabels, grid, isTimeSeries);
		// --
		initClassification(rawData, features, nLabels, grid);
		// --
		initMenu(labelingComponent.getActions());
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}

	private <R extends RealType<R>> void initClassification(RandomAccessibleInterval<R> rawData, List<? extends RandomAccessibleInterval<F>> features, int nLabels, CellGrid grid) {
		final Interval interval = new FinalInterval(rawData);
		final int nDim = rawData.numDimensions();

		final RandomAccessibleInterval<F> featuresConcatenated = concatenateFeatures(features, nDim);
		ColorMapColorProvider colorProvider = labelingComponent.colorProvider();
		TLongIntHashMap labelingMap = labelingComponent.labelingMap();
		final TrainClassifier<F> trainer = initTrainer(nLabels, grid, interval, colorProvider, labelingMap, featuresConcatenated);
		labelingComponent.addAction(trainer, "ctrl shift T");
		initSaveClassifierAction();
		initLoadClassifierAction(trainer);
		final int nFeatures = ( int ) featuresConcatenated.dimension( nDim );
		initMouseWheelSelection(nFeatures);
		bdvAddFeatures();
	}

	private SharedQueue initQueue() {
		final int numFetcherThreads = Runtime.getRuntime().availableProcessors();
		return new SharedQueue( numFetcherThreads );
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("ATLAS");
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	private void initMenu(List<AbstractNamedAction> actions) {
		MenuBar bar = new MenuBar();
		JMenu others = new JMenu("others");
		others.add(newMenuItem("Show Feature", () -> selectFeature()));
		bar.add(others);
		actions.forEach(bar::add);
		frame.setJMenuBar(bar);
	}

	private JMenuItem newMenuItem(String title, Runnable runnable) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(a -> runnable.run());
		return item;
	}

	private TrainClassifier<F> initTrainer(int nLabels, CellGrid grid, Interval interval, ColorMapColorProvider colorProvider, TLongIntHashMap labelingMap, RandomAccessibleInterval<F> featuresConcatenated) {
		final RandomAccessibleContainer<VolatileARGBType> container = initPredictionLayer(interval, grid.numDimensions());
		final UpdatePrediction.CacheOptions cacheOptions = new UpdatePrediction.CacheOptions( "prediction", grid, queue);
		final ClassifyingCellLoader< F > classifyingLoader = new ClassifyingCellLoader<>(featuresConcatenated, this.classifier);
		final UpdatePrediction< F > predictionAdder = new UpdatePrediction<>(bdvHandle.getViewerPanel(), classifyingLoader, colorProvider, cacheOptions, container);
		final ArrayList< String > classes = new ArrayList<>();
		for (int i = 1; i <= nLabels; ++i )
			classes.add( "" + i );

		final TrainClassifier< F > trainer = new TrainClassifier<F>(this.classifier, labelingMap, featuresConcatenated, classes );
		trainer.addListener( predictionAdder );
		return trainer;
	}

	private RandomAccessibleContainer<VolatileARGBType> initPredictionLayer(Interval interval, int nDim) {
		// add prediction layer
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), nDim );
		final RandomAccessibleContainer< VolatileARGBType > container = new RandomAccessibleContainer<>( emptyPrediction );
		BdvFunctions.show( container, interval, "prediction", BdvOptions.options().addTo( bdvHandle ) );
		return container;
	}

	private RandomAccessibleInterval<F> concatenateFeatures(List<? extends RandomAccessibleInterval<F>> features, int nDim) {
		slices = features.stream().flatMap(feature ->
			feature.numDimensions() == nDim ?
					Stream.of(feature) :
					RevampUtils.slices(feature).stream()
		).collect(Collectors.toList());
		return Views.stack(slices);
	}

	private void initSaveClassifierAction() {
		final SerializeClassifier saveDialogAction = new SerializeClassifier( "classifier-serializer", bdvHandle.getViewerPanel(), this.classifier);
		labelingComponent.addAction(saveDialogAction, "ctrl S");
	}

	private void initLoadClassifierAction(TrainClassifier<F> trainer) {
		final DeserializeClassifier loadDialogAction = new DeserializeClassifier(bdvHandle.getViewerPanel(), this.classifier, trainer.getListeners() );
		labelingComponent.addAction(loadDialogAction, "ctrl O");
	}

	private void initMouseWheelSelection(int nFeatures) {
		final MouseWheelChannelSelector mouseWheelSelector = new MouseWheelChannelSelector(bdvHandle.getViewerPanel(), 2, nFeatures );
		labelingComponent.addBehaviour(mouseWheelSelector, "mouseweheel selector", "shift F scroll");
		labelingComponent.addBehaviour(mouseWheelSelector.getOverlay(), "feature selector overlay", "shift F");
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( mouseWheelSelector.getOverlay() );
	}

	private void bdvAddFeatures() {
		featureContainer = new RandomAccessibleContainer<>(tryWrapAsVolatile(slices.get(0)));
		final BdvStackSource source = BdvFunctions.show(Views.interval(featureContainer, slices.get(0)), "feature", BdvOptions.options().addTo(bdvHandle));
		source.setDisplayRange( 0, 255 );
		source.setActive( false );
	}

	public <T> RandomAccessibleInterval<T> tryWrapAsVolatile(RandomAccessibleInterval<T> rai) {
		try
		{
			return AtlasUtils.uncheckedCast(VolatileViews.wrapAsVolatile(rai, queue));
		}
		catch ( final IllegalArgumentException e )
		{
			return rai;
		}
	}

	public void selectFeature() {
		int index = (Integer) JOptionPane.showInputDialog(null, "Index of Feature", "Select Feature",
				JOptionPane.PLAIN_MESSAGE, null, IntStream.rangeClosed(0, slices.size() - 1).mapToObj(x -> x).toArray(), 0);
		featureContainer.setSource(tryWrapAsVolatile(slices.get(index)));
		bdvHandle.getViewerPanel().requestRepaint();
	}
}
