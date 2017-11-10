package net.imglib2.atlas;

import bdv.util.BdvStackSource;
import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import hr.irb.fastRandomForest.FastRandomForest;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.atlas.actions.BatchSegmentAction;
import net.imglib2.atlas.actions.ChangeFeatureSettingsAction;
import net.imglib2.atlas.actions.ClassifierIoAction;
import net.imglib2.atlas.actions.LabelingIoAction;
import net.imglib2.atlas.actions.OpenImageAction;
import net.imglib2.atlas.actions.OrthogonalView;
import net.imglib2.atlas.actions.SegmentationSave;
import net.imglib2.atlas.actions.SelectClassifier;
import net.imglib2.atlas.actions.ZAxisScaling;
import net.imglib2.atlas.classification.Classifier;
import net.imglib2.atlas.classification.PredictionLayer;
import net.imglib2.atlas.classification.TrainClassifier;
import net.imglib2.atlas.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.atlas.inputimage.DefaultInputImage;
import net.imglib2.atlas.inputimage.InputImage;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.atlas.plugin.MeasureConnectedComponents;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.ui.OverlayRenderer;
import org.scijava.Context;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SegmentationComponent {

	private final JPanel panel = initPanel();

	private final Classifier classifier;

	private final JFrame dialogBoxOwner;

	private SharedQueue queue = new SharedQueue(Runtime.getRuntime().availableProcessors());

	private LabelingComponent labelingComponent;

	private FeatureStack featureStack;

	private MyExtensible extensible = new MyExtensible();

	private final Context context;

	private final InputImage inputImage;

	public SegmentationComponent(Context context, JFrame dialogBoxOwner, RandomAccessibleInterval<? extends NumericType<?>> image) {
		this(context, dialogBoxOwner, new DefaultInputImage(image));
	}

	public SegmentationComponent(Context context, JFrame dialogBoxOwner, InputImage image) {
		this.dialogBoxOwner = dialogBoxOwner;
		this.inputImage = image;
		this.context = context;
		RandomAccessibleInterval<? extends NumericType<?>> displayImage = image.displayImage();
		Labeling labeling = new Labeling(Arrays.asList("background", "foreground"), displayImage);
		labelingComponent = new LabelingComponent(dialogBoxOwner, displayImage, labeling, false);
		panel.add(labelingComponent.getComponent());
		// --
		GlobalSettings globalSettings = new GlobalSettings(inputImage.getChannelSetting(), inputImage.getSpatialDimensions(), 1.0, 16.0, 1.0);
		OpService ops = context.service(OpService.class);
		FeatureSettings setting = new FeatureSettings(globalSettings, SingleFeatures.identity(), GroupedFeatures.gauss());
		classifier = new TrainableSegmentationClassifier(ops, new FastRandomForest(), labeling.getLabels(), setting );
		featureStack = new FeatureStack(extensible, displayImage, classifier, false);
		initClassification();
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

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
		new ChangeFeatureSettingsAction(extensible, classifier);
		MeasureConnectedComponents.addAction(extensible);
	}

	private static JPanel initPanel() {
		JPanel panel = new JPanel();
		panel.setSize(100, 100);
		panel.setLayout(new BorderLayout());
		return panel;
	}

	public void setLabeling(Labeling labeling) {
		labelingComponent.setLabeling(labeling);
	}

	public Labeling getLabeling() {
		return labelingComponent.getLabeling();
	}

	public ActionMap getActions() {
		return labelingComponent.getActions();
	}

	public <T extends IntegerType<T> & NativeType<T>> RandomAccessibleInterval<T> getSegmentation(T type) {
		RandomAccessibleInterval<T> labels =
				context.service(OpService.class).create().img(inputImage.displayImage(), type);
		classifier.segment(inputImage.displayImage(), labels);
		return labels;
	}

	public RandomAccessibleInterval<FloatType> getPrediction() {
		RandomAccessibleInterval<FloatType> prediction =
				context.service(OpService.class).create().img(
						RevampUtils.appendDimensionToInterval(inputImage.displayImage(), 0, 1),
						new FloatType());
		classifier.predict(inputImage.displayImage(), prediction);
		return prediction;
	}

	private class MyExtensible implements Extensible {

		@Override
		public Context context() {
			return context;
		}

		@Override
		public void repaint() {
			labelingComponent.requestRepaint();
		}

		@Override
		public void addAction(String title, String command, Runnable action, String keyStroke) {
			RunnableAction a = new RunnableAction(title, action);
			a.putValue(Action.ACTION_COMMAND_KEY, command);
			a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
			addAction(a);
		}

		@Override
		public void addAction(AbstractNamedAction action) {
			labelingComponent.addAction(action);
		}

		@Override
		public < T, V extends Volatile< T >> RandomAccessibleInterval< V > wrapAsVolatile(
				RandomAccessibleInterval<T> img)
		{
			return VolatileViews.wrapAsVolatile( img, queue );
		}

		@Override
		public Object viewerSync() {
			return labelingComponent.viewerSync();
		}

		@Override
		public <T extends NumericType<T>> BdvStackSource<T> addLayer(RandomAccessibleInterval<T> interval, String prediction) {
			return labelingComponent.addLayer(interval, prediction);
		}

		@Override
		public Component dialogParent() {
			return dialogBoxOwner;
		}

		@Override
		public void addBehaviour(Behaviour behaviour, String name, String defaultTriggers) {
			labelingComponent.addBehaviour(behaviour, name, defaultTriggers);
		}

		@Override
		public void addOverlayRenderer(OverlayRenderer overlay) {
			labelingComponent.addOverlayRenderer(overlay);
		}

		@Override
		public void displayRepaint() {
			labelingComponent.displayRepaint();
		}

		@Override
		public Labeling getLabeling() {
			return labelingComponent.getLabeling();
		}

		@Override
		public void setLabeling(Labeling labeling) {
			labelingComponent.setLabeling(labeling);
		}
	}
}
