package net.imglib2.atlas;

import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import hr.irb.fastRandomForest.FastRandomForest;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.atlas.actions.BatchSegmentAction;
import net.imglib2.atlas.actions.ChangeFeatureSettingsAction;
import net.imglib2.atlas.actions.ClassifierIoAction;
import net.imglib2.atlas.actions.LabelingIoAction;
import net.imglib2.atlas.actions.OpenImageAction;
import net.imglib2.atlas.actions.OrthogonalView;
import net.imglib2.atlas.actions.SegmentationSave;
import net.imglib2.atlas.actions.SelectClassifier;
import net.imglib2.atlas.actions.SetLabelsAction;
import net.imglib2.atlas.actions.ZAxisScaling;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.plugin.MeasureConnectedComponents;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.OverlayRenderer;
import org.scijava.Context;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A component that supports labeling an image.
 *
 * @author Matthias Arzt
 */
public class MainFrame {

	private JFrame frame = initFrame();

	private final Classifier classifier;

	private SharedQueue queue = new SharedQueue(Runtime.getRuntime().availableProcessors());

	private LabelingComponent labelingComponent;

	private FeatureStack featureStack;

	private Extensible extensible = new Extensible();

	private final Context context;

	private final Preferences preferences;

	private final InputImage inputImage;

	public static MainFrame open(String filename)
	{
		return open(null, filename);
	}

	public static MainFrame open(Context context, String filename) {
		final Context context2 = (context == null) ? new Context() : context;
		Dataset dataset = RevampUtils.wrapException( () -> context2.service(DatasetIOService.class).open(filename) );
		return new MainFrame(context2, dataset);
	}

	public MainFrame(final Context context, final Dataset dataset)
	{
		this.context = context;
		this.preferences = new Preferences(context);
		inputImage = new InputImage(dataset);
		RandomAccessibleInterval<? extends NumericType<?>> rawData = inputImage.displayImage();
		List<String> classLabels = preferences.getDefaultLabels();
		Labeling labeling = new Labeling(classLabels, rawData);
		labeling.setAxes(inputImage.axes());
		labelingComponent = new LabelingComponent(frame, rawData, labeling, false);
		// --
		GlobalSettings globalSettings = new GlobalSettings(inputImage.getChannelSetting(), inputImage.getSpatialDimensions(), 1.0, 16.0, 1.0);
		OpService ops = context.service(OpService.class);
		FeatureSettings setting = new FeatureSettings(globalSettings, SingleFeatures.identity(), GroupedFeatures.gauss());
		classifier = new TrainableSegmentationClassifier(ops, new FastRandomForest(), classLabels, setting );
		featureStack = new FeatureStack(extensible, rawData, classifier, false);
		initClassification();
		// --
		frame.setJMenuBar(new MenuBar(labelingComponent.getActions()));
		frame.add(labelingComponent.getComponent());
		frame.setVisible(true);
	}


	private void initClassification() {
		new TrainClassifier(extensible, classifier, () -> labelingComponent.getLabeling(), featureStack.compatibleOriginal());
		PredictionLayer predictionLayer = new PredictionLayer(extensible, labelingComponent.colorProvider(), classifier, featureStack);
		new ClassifierIoAction(extensible, this.classifier);
		new FeatureLayer(extensible, featureStack);
		new LabelingIoAction(extensible, labelingComponent, inputImage);
		new SegmentationSave(extensible, predictionLayer);
		new OpenImageAction(extensible);
		new ZAxisScaling(extensible, labelingComponent.sourceTransformation());
		new OrthogonalView(extensible, new AffineTransform3D());
		new SelectClassifier(extensible, classifier);
		new BatchSegmentAction(extensible, classifier);
		new SetLabelsAction(extensible, preferences);
		new ChangeFeatureSettingsAction(extensible, classifier);
		MeasureConnectedComponents.addAction(extensible);
	}

	private JFrame initFrame() {
		JFrame frame = new JFrame("BDV Labkit");
		frame.setBounds( 50, 50, 1200, 900 );
		return frame;
	}

	public class Extensible {

		private Extensible() {

		}

		public Context context() {
			return context;
		}

		public void repaint() {
			labelingComponent.requestRepaint();
		}

		public void addAction(String title, String command, Runnable action, String keyStroke) {
			RunnableAction a = new RunnableAction(title, action);
			a.putValue(Action.ACTION_COMMAND_KEY, command);
			a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
			addAction(a);
		}

		public void addAction(AbstractNamedAction action) {
			labelingComponent.addAction(action);
		}

		public < T, V extends Volatile< T >> RandomAccessibleInterval< V > wrapAsVolatile(
				RandomAccessibleInterval<T> img)
		{
			return VolatileViews.wrapAsVolatile( img, queue );
		}

		public Object viewerSync() {
			return labelingComponent.viewerSync();
		}

		public <T extends NumericType<T>> BdvStackSource<T> addLayer(RandomAccessibleInterval<T> interval, String prediction) {
			return labelingComponent.addLayer(interval, prediction);
		}

		public Component dialogParent() {
			return frame;
		}

		public void addBehaviour(Behaviour behaviour, String name, String defaultTriggers) {
			labelingComponent.addBehaviour(behaviour, name, defaultTriggers);
		}

		public void addOverlayRenderer(OverlayRenderer overlay) {
			labelingComponent.addOverlayRenderer(overlay);
		}

		public void displayRepaint() {
			labelingComponent.displayRepaint();
		}

		public Labeling getLabeling() {
			return labelingComponent.getLabeling();
		}

		public void setLabeling(Labeling labeling) {
			labelingComponent.setLabeling(labeling);
		}
	}
}
