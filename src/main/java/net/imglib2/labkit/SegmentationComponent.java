package net.imglib2.labkit;

import java.awt.Component;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.actions.AddLabelingIoAction;
import net.imglib2.labkit.actions.BatchSegmentAction;
import net.imglib2.labkit.actions.ChangeFeatureSettingsAction;
import net.imglib2.labkit.actions.ClassifierIoAction;
import net.imglib2.labkit.actions.LabelingIoAction;
import net.imglib2.labkit.actions.OpenImageAction;
import net.imglib2.labkit.actions.OrthogonalView;
import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.actions.SegmentationSave;
import net.imglib2.labkit.actions.SelectClassifier;
import net.imglib2.labkit.classification.Classifier;
import net.imglib2.labkit.classification.PredictionLayer;
import net.imglib2.labkit.classification.TrainClassifier;
import net.imglib2.labkit.classification.weka.TimeSeriesClassifier;
import net.imglib2.labkit.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmentationPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.trainable_segmention.pixel_feature.filter.GroupedFeatures;
import net.imglib2.trainable_segmention.pixel_feature.filter.SingleFeatures;
import net.imglib2.trainable_segmention.pixel_feature.settings.FeatureSettings;
import net.imglib2.trainable_segmention.pixel_feature.settings.GlobalSettings;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;

import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import hr.irb.fastRandomForest.FastRandomForest;

public class SegmentationComponent implements AutoCloseable {

	private final JPanel panel;

	private final boolean fixedLabels;

	private Classifier classifier;

	private final JFrame dialogBoxOwner;

	private LabelingComponent labelingComponent;

	private ImageLabelingModel model;

	private final Context context;

	private final InputImage inputImage;

	private SegmentationModel segmentationModel;

	private SegmentationResultsModel segmentationResultsModel;

	public SegmentationComponent(Context context,
			JFrame dialogBoxOwner,
			RandomAccessibleInterval<? extends NumericType<?>> image,
			boolean isTimeSeries ) {
		this(context, dialogBoxOwner, initInputImage(image, isTimeSeries), new Labeling(Arrays.asList("background", "foreground"), image), true);
	}

	private static DefaultInputImage initInputImage(RandomAccessibleInterval<? extends NumericType<?>> image, boolean isTimeSeries) {
		DefaultInputImage defaultInputImage = new DefaultInputImage(image);
		defaultInputImage.setTimeSeries(isTimeSeries);
		return defaultInputImage;
	}

	public SegmentationComponent(Context context, JFrame dialogBoxOwner, InputImage image, Labeling labeling, boolean fixedLabels) {
		this.dialogBoxOwner = dialogBoxOwner;
		this.inputImage = image;
		this.context = context;
		this.fixedLabels = fixedLabels;
		model = new ImageLabelingModel( image.displayImage(), image.scaling(), labeling, inputImage.isTimeSeries());
		initModels();
		labelingComponent = new LabelingComponent(dialogBoxOwner, model);
		labelingComponent.addBdvLayer( new PredictionLayer( segmentationResultsModel ) );
		initActions();
		JPanel leftPanel = initLeftPanel();
		JPanel bottomPanel = initBottomPanel();
		this.panel = initPanel( leftPanel, labelingComponent.getComponent(), bottomPanel );
	}

	private void initModels()
	{
		classifier = initClassifier( context );
		segmentationModel = new SegmentationModel( model, classifier );
		segmentationResultsModel = new SegmentationResultsModel( segmentationModel );
	}

	private Classifier initClassifier( Context context )
	{
		GlobalSettings globalSettings = new GlobalSettings(inputImage.getChannelSetting(), inputImage.getSpatialDimensions(), 1.0, 16.0, 1.0);
		OpService ops = context.service(OpService.class);
		FeatureSettings setting = new FeatureSettings(globalSettings, SingleFeatures.identity(), GroupedFeatures.gauss());
		TrainableSegmentationClassifier classifier1 = new TrainableSegmentationClassifier(ops, new FastRandomForest(), model.labeling().get().getLabels(), setting);
		return inputImage.isTimeSeries() ? new TimeSeriesClassifier(classifier1) : classifier1;
	}

	private void initActions()
	{
		MyExtensible extensible = new MyExtensible();
		new TrainClassifier(extensible, segmentationModel );
		new ClassifierIoAction(extensible, this.classifier);
		new LabelingIoAction(extensible, model.labeling(), inputImage);
		new AddLabelingIoAction(extensible, model.labeling());
		new SegmentationSave(extensible, segmentationResultsModel );
		new OpenImageAction(extensible);
		new OrthogonalView(extensible, model);
		new SelectClassifier(extensible, classifier);
		new BatchSegmentAction(extensible, classifier);
		new ChangeFeatureSettingsAction(extensible, classifier);
		new SegmentationAsLabelAction(extensible, segmentationResultsModel, model.labeling());
		MeasureConnectedComponents.addAction(extensible, model);
	}

	private JPanel initLeftPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("","[grow]","[][][grow][][]"));
		ActionMap actions = getActions();
		addCheckbox(panel, actions.get("Image"));
		addCheckbox(panel, actions.get("Labeling"));
		panel.add(new LabelPanel(dialogBoxOwner, new ColoredLabelsModel( model ), fixedLabels).getComponent(), "grow, wrap");
		addCheckbox(panel, actions.get("Segmentation"));
		panel.add(new SegmentationPanel(dialogBoxOwner, segmentationResultsModel, fixedLabels).getComponent(), "");
		return panel;
	}

	private void addCheckbox(JPanel panel2, Action image) {
		JCheckBox checkbox = new JCheckBox(image);
		checkbox.setFocusable(false);
		panel2.add(checkbox, "wrap");
	}

	private JPanel initBottomPanel() {
		JPanel panel = new JPanel();
		ActionMap actions = getActions();
		panel.add( trainClassifierButton( actions ), "grow, wrap");
		panel.add( selectClassifierButton( actions ), "grow, wrap");
		panel.add( changeFeatureSettingsButton( actions ), "grow, wrap");
		return panel;
	}

	private JButton trainClassifierButton( ActionMap actions )
	{
		JButton button = new JButton( actions.get( "Train Classifier" ) );
		button.setFocusable( false );
		return button;
	}

	private JButton selectClassifierButton( ActionMap actions )
	{
		JButton button = new JButton( actions.get( "Select Classification Algorithm ..." ) );
		button.setFocusable( false );
		return button;
	}

	private JButton changeFeatureSettingsButton( ActionMap actions )
	{
		JButton button = new JButton( actions.get( "Change Feature Settings ..." ) );
		button.setFocusable( false );
		return button;
	}

	private JPanel initPanel( JComponent left, JComponent right, JComponent bottom )
	{
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("fill","[grow]","[grow][]"));
		JSplitPane center = new JSplitPane();
		center.setSize(100, 100);
		center.setOneTouchExpandable(true);
		center.setLeftComponent( left );
		center.setRightComponent( right );
		panel.add( center, "wrap, grow" );
		panel.add( bottom );
		return panel;
	}

	public JComponent getComponent() {
		return panel;
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

	public boolean isTrained()
	{
		return classifier.isTrained();
	}

	@Override
	public void close()
	{
		labelingComponent.close();
	}

	private class MyExtensible implements Extensible {

		@Override
		public Context context() {
			return context;
		}

		@Override
		public void addAction(String title, String command, Runnable action, String keyStroke) {
			RunnableAction a = new RunnableAction(title, action);
			a.putValue(Action.ACTION_COMMAND_KEY, command);
			a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
			labelingComponent.addAction( a );
		}

		@Override
		public Component dialogParent() {
			return dialogBoxOwner;
		}
	}
}
