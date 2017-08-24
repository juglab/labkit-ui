package net.imglib2.atlas;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import hr.irb.fastRandomForest.FastRandomForest;
import net.imglib2.*;
import net.imglib2.algorithm.features.FeatureGroup;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.GlobalSettings;
import net.imglib2.algorithm.features.gui.FeatureSettingsGui;
import net.imglib2.atlas.actions.ClassifierSaveAndLoad;
import net.imglib2.atlas.actions.LabelingSaveAndLoad;
import net.imglib2.atlas.actions.OpenImageAction;
import net.imglib2.atlas.actions.SegmentationSave;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.ui.OverlayRenderer;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.imglib2.algorithm.features.GroupedFeatures;
import net.imglib2.algorithm.features.SingleFeatures;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private JFrame frame = initFrame();

	private Classifier classifier;

	private SharedQueue queue = new SharedQueue(Runtime.getRuntime().availableProcessors());

	private BdvHandle bdvHandle;

	private LabelingComponent labelingComponent;

	private FeatureStack featureStack;

	private List<String> classLabels = Arrays.asList("foreground", "background");

	private Extensible extensible = new Extensible();

	public <R extends NumericType<R>>
	MainFrame(final RandomAccessibleInterval<R> rawData,
			  final boolean isTimeSeries)
	{
		labelingComponent = new LabelingComponent(frame);

		bdvHandle = labelingComponent.trainClassifier(rawData, classLabels, isTimeSeries);
		// --
		GlobalSettings globalSettings = new GlobalSettings(getImageType(rawData), 1.0, 16.0, 1.0);
		FeatureGroup featureGroup = Features.group(new SingleFeatures(globalSettings).identity(), new GroupedFeatures(globalSettings).gauss());
		classifier = new TrainableSegmentationClassifier(FastRandomForest::new, classLabels, featureGroup);
		featureStack = new FeatureStack(rawData, classifier, isTimeSeries);
		initClassification();
		// --
		initMenu(labelingComponent.getActions());
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}

	private GlobalSettings.ImageType getImageType(RandomAccessibleInterval<?> rawData) {
		Object firstElement = rawData.randomAccess().get();
		if(firstElement instanceof RealType)
			return GlobalSettings.ImageType.GRAY_SCALE;
		if(firstElement instanceof ARGBType)
			return GlobalSettings.ImageType.COLOR;
		throw new RuntimeException();
	}

	private void initClassification() {
		new TrainClassifier<>(extensible, classifier, () -> labelingComponent.getLabeling(), featureStack.block());
		PredictionLayer predictionLayer = new PredictionLayer(extensible, labelingComponent.colorProvider(), classifier, featureStack);
		new ClassifierSaveAndLoad(extensible, this.classifier);
		new FeatureLayer(extensible, featureStack);
		new LabelingSaveAndLoad(extensible, labelingComponent);
		new SegmentationSave(extensible, predictionLayer);
		new OpenImageAction(extensible);
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("ATLAS");
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	private void initMenu(List<AbstractNamedAction> actions) {
		MenuBar bar = new MenuBar();
		JMenu others = new JMenu("others");
		others.add(newMenuItem("Change Feature Settings", this::changeFeatureSettings));
		bar.add(others);
		actions.forEach(bar::add);
		frame.setJMenuBar(bar);
	}

	private void changeFeatureSettings() {
		Optional<FeatureGroup> fg = FeatureSettingsGui.show(classifier.features());
		if(!fg.isPresent())
			return;
		classifier.reset(fg.get(), classLabels);
	}

	private JMenuItem newMenuItem(String title, Runnable runnable) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(a -> runnable.run());
		return item;
	}

	public class Extensible {

		private Extensible() {

		}

		public void repaint() {
			bdvHandle.getViewerPanel().requestRepaint();
		}

		public void addAction(AbstractNamedAction action, String keyStroke) {
			labelingComponent.addAction(action, keyStroke);
		}

		public < T, V extends Volatile< T >> RandomAccessibleInterval< V > wrapAsVolatile(
				RandomAccessibleInterval<T> img)
		{
			return VolatileViews.wrapAsVolatile( img, queue );
		}

		public Object viewerSync() {
			return bdvHandle.getViewerPanel();
		}

		public <T extends NumericType<T>> BdvStackSource<T> addLayer(RandomAccessibleInterval<T> interval, String prediction) {
			return BdvFunctions.show(interval, prediction, BdvOptions.options().addTo(bdvHandle));
		}

		public Component dialogParent() {
			return frame;
		}

		public void addBehaviour(Behaviour behaviour, String name, String defaultTriggers) {
			labelingComponent.addBehaviour(behaviour, name, defaultTriggers);
		}

		public void addOverlayRenderer(OverlayRenderer overlay) {
			bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer(overlay);
		}

		public void displayRepaint() {
			bdvHandle.getViewerPanel().getDisplay().repaint();
		}
	}
}
