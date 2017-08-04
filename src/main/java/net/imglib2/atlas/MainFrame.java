package net.imglib2.atlas;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import gnu.trove.map.hash.TLongIntHashMap;
import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.gui.FeatureSettingsGui;
import net.imglib2.atlas.actions.DeserializeClassifier;
import net.imglib2.atlas.actions.SerializeClassifier;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.atlas.color.ColorMapColorProvider;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static net.imglib2.algorithm.features.GroupedFeatures.*;
import static net.imglib2.algorithm.features.SingleFeatures.*;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private JFrame frame = initFrame();

	private Classifier classifier;

	private SharedQueue queue = initQueue();

	private BdvHandle bdvHandle;

	private LabelingComponent labelingComponent;

	private RandomAccessibleContainer<FloatType> featureContainer;

	private FeatureStack featureStack;

	private List<String> classLabels = Arrays.asList("foreground", "background");

	public < R extends RealType< R > >
	void trainClassifier(
			final RandomAccessibleInterval<R> rawData,
			final CellGrid grid,
			final boolean isTimeSeries) throws IOException
	{
		labelingComponent = new LabelingComponent(frame);

		int nLabels = classLabels.size();
		bdvHandle = labelingComponent.trainClassifier(rawData, nLabels, grid, isTimeSeries);
		// --
		FeatureGroup featureGroup = Features.group(SingleFeatures.identity(), GroupedFeatures.gauss());
		this.classifier = new TrainableSegmentationClassifier(FastRandomForest::new, classLabels, featureGroup);
		featureStack = new FeatureStack(toFloatType(rawData), classifier, grid);
		initClassification(rawData, nLabels, grid);
		// --
		initMenu(labelingComponent.getActions());
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}

	private <R extends RealType<R>> RandomAccessibleInterval<FloatType> toFloatType(RandomAccessibleInterval<R> in) {
		return Converters.convert(in, new RealFloatConverter<>(), new FloatType() );
	}

	private <R extends RealType<R>> void initClassification(RandomAccessibleInterval<R> rawData, int nLabels, CellGrid grid) {
		final Interval interval = new FinalInterval(rawData);

		ColorMapColorProvider colorProvider = labelingComponent.colorProvider();
		TLongIntHashMap labelingMap = labelingComponent.labelingMap();
		final TrainClassifier<FloatType> trainer = new TrainClassifier<>(this.classifier, labelingMap, featureStack.block());
		initPredictionLayer(grid, interval, colorProvider);
		labelingComponent.addAction(trainer, "ctrl shift T");
		initSaveClassifierAction();
		initLoadClassifierAction(trainer);
		initMouseWheelSelection(featureStack.filter().count());
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
		others.add(newMenuItem("Show Feature", this::selectFeature));
		others.add(newMenuItem("Change Feature Settings", this::changeFeatureSettings));
		bar.add(others);
		actions.forEach(bar::add);
		frame.setJMenuBar(bar);
	}

	private void changeFeatureSettings() {
		Optional<FeatureGroup> fg = FeatureSettingsGui.show();
		if(!fg.isPresent())
			return;
		classifier.reset(fg.get(), classLabels);
	}

	private JMenuItem newMenuItem(String title, Runnable runnable) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(a -> runnable.run());
		return item;
	}

	private void initPredictionLayer(CellGrid grid, Interval interval, ColorMapColorProvider colorProvider) {
		// add prediction layer
		RandomAccessible prediction = new PredictionLayer(grid, colorProvider, classifier, queue, featureStack.block(), bdvHandle.getViewerPanel()).prediction();
		BdvFunctions.show(prediction, interval, "prediction", BdvOptions.options().addTo( bdvHandle ) );
	}

	private void initSaveClassifierAction() {
		final SerializeClassifier saveDialogAction = new SerializeClassifier( "classifier-serializer", bdvHandle.getViewerPanel(), this.classifier);
		labelingComponent.addAction(saveDialogAction, "ctrl S");
	}

	private void initLoadClassifierAction(TrainClassifier<FloatType> trainer) {
		final DeserializeClassifier loadDialogAction = new DeserializeClassifier(bdvHandle.getViewerPanel(), this.classifier);
		labelingComponent.addAction(loadDialogAction, "ctrl O");
	}

	private void initMouseWheelSelection(int nFeatures) {
		final MouseWheelChannelSelector mouseWheelSelector = new MouseWheelChannelSelector(bdvHandle.getViewerPanel(), 2, nFeatures );
		labelingComponent.addBehaviour(mouseWheelSelector, "mouseweheel selector", "shift F scroll");
		labelingComponent.addBehaviour(mouseWheelSelector.getOverlay(), "feature selector overlay", "shift F");
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( mouseWheelSelector.getOverlay() );
	}

	private void bdvAddFeatures() {
		featureContainer = new RandomAccessibleContainer<>(tryWrapAsVolatile(featureStack.slices().get(0)));
		final BdvStackSource source = BdvFunctions.show(Views.interval(featureContainer, featureStack.slices().get(0)), "feature", BdvOptions.options().addTo(bdvHandle));
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
		List<RandomAccessibleInterval<FloatType>> slices = featureStack.slices();
		List<String> names = featureStack.filter().attributeLabels();
		Object[] objects = IntStream.range(0, slices.size()).mapToObj(i -> new NamedValue<>(names.get(i), slices.get(i))).toArray();
		NamedValue<RandomAccessibleInterval<FloatType>> selected =
				(NamedValue<RandomAccessibleInterval<FloatType>>) JOptionPane.showInputDialog(null, "Index of Feature", "Select Feature",
				JOptionPane.PLAIN_MESSAGE, null, objects, 0);
		featureContainer.setSource(tryWrapAsVolatile(selected.get()));
		bdvHandle.getViewerPanel().requestRepaint();
	}

	class NamedValue<T> {
		private final String name;
		private final T value;

		NamedValue(String name, T value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String toString() {
			return name;
		}

		public T get() {
			return value;
		}
	}
}
