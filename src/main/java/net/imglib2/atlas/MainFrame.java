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
		final Interval interval = new FinalInterval(rawData);
		final int nDim = rawData.numDimensions();

		final RandomAccessibleInterval<F> featuresConcatenated = concatenateFeatures(features, nDim);
		ColorMapColorProvider colorProvider = labelingComponent.colorProvider();
		TLongIntHashMap labelingMap = labelingComponent.labelingMap();
		final TrainClassifier<F> trainer = initTrainer(nLabels, grid, interval, colorProvider, labelingMap, featuresConcatenated);
		addAction(trainer, "ctrl shift T");
		initSaveClassifierAction();
		initLoadClassifierAction(trainer);
		final int nFeatures = ( int ) featuresConcatenated.dimension( nDim );
		initMouseWheelSelection(nFeatures);
		bdvAddFeatures(bdvHandle, features);
		// --
		initMenu(labelingComponent.getActions());
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}

	private void addAction(AbstractNamedAction action, String s) {
		labelingComponent.addAction(action, s);
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
		actions.forEach(bar::add);
		frame.setJMenuBar(bar);
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
		return Views.concatenate( nDim, features.stream().map(f -> f.numDimensions() == nDim ? Views.addDimension( f, 0, 0 ) : f ).collect( Collectors.toList() ) );
	}

	private void initSaveClassifierAction() {
		final SerializeClassifier saveDialogAction = new SerializeClassifier( "classifier-serializer", bdvHandle.getViewerPanel(), this.classifier);
		addAction(saveDialogAction, "ctrl S");
	}

	private void initLoadClassifierAction(TrainClassifier<F> trainer) {
		final DeserializeClassifier loadDialogAction = new DeserializeClassifier(bdvHandle.getViewerPanel(), this.classifier, trainer.getListeners() );
		addAction(loadDialogAction, "ctrl O");
	}

	private void initMouseWheelSelection(int nFeatures) {
		final MouseWheelChannelSelector mouseWheelSelector = new MouseWheelChannelSelector(bdvHandle.getViewerPanel(), 2, nFeatures );
		labelingComponent.addBehaviour(mouseWheelSelector, "mouseweheel selector", "shift F scroll");
		labelingComponent.addBehaviour(mouseWheelSelector.getOverlay(), "feature selector overlay", "shift F");
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( mouseWheelSelector.getOverlay() );
	}

	private void bdvAddFeatures(BdvHandle bdv, List<? extends RandomAccessibleInterval<F>> features) {
		for ( int feat = 0; feat < features.size(); ++feat )
		{
			final BdvStackSource source = BdvFunctions.show(tryWrapAsVolatile(features.get(feat)), "feature " + (feat + 1), BdvOptions.options().addTo(bdv));
			source.setDisplayRange( 0, 255 );
			source.setActive( false );
		}
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
}
