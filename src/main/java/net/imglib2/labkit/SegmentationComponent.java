package net.imglib2.labkit;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.actions.AddLabelingIoAction;
import net.imglib2.labkit.actions.BatchSegmentAction;
import net.imglib2.labkit.actions.ClassifierIoAction;
import net.imglib2.labkit.actions.LabelingIoAction;
import net.imglib2.labkit.actions.OpenImageAction;
import net.imglib2.labkit.actions.OrthogonalView;
import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.actions.SegmentationSave;
import net.imglib2.labkit.actions.SelectClassifier;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.segmentation.weka.TimeSeriesSegmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.VisibilityPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Arrays;

public class SegmentationComponent implements AutoCloseable {

	private final JSplitPane panel;

	private final boolean fixedLabels;

	private Segmenter segmenter;

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
		initModels(image, labeling);
		labelingComponent = new LabelingComponent(dialogBoxOwner, model);
		labelingComponent.addBdvLayer( new PredictionLayer( segmentationResultsModel ) );
		initActions();
		JPanel leftPanel = initLeftPanel();
		this.panel = initPanel( leftPanel, labelingComponent.getComponent() );
	}

	private void initModels( InputImage image, Labeling labeling )
	{
		model = new ImageLabelingModel( image.displayImage(), labeling, inputImage.isTimeSeries());
		segmenter = initClassifier( context );
		segmentationModel = new SegmentationModel( image.displayImage(), model, segmenter );
		segmentationResultsModel = new SegmentationResultsModel( segmentationModel );
	}

	private Segmenter initClassifier( Context context )
	{
		TrainableSegmentationSegmenter classifier1 = new TrainableSegmentationSegmenter(context, inputImage);
		return inputImage.isTimeSeries() ? new TimeSeriesSegmenter(classifier1) : classifier1;
	}

	private void initActions()
	{
		MyExtensible extensible = new MyExtensible();
		new TrainClassifier(extensible, segmentationModel );
		new ClassifierIoAction(extensible, this.segmenter );
		new LabelingIoAction(extensible, model.labeling(), inputImage);
		new AddLabelingIoAction(extensible, model.labeling());
		new SegmentationSave(extensible, segmentationResultsModel );
		new OpenImageAction(extensible);
		new OrthogonalView(extensible, model);
		new SelectClassifier(extensible, segmenter );
		new BatchSegmentAction(extensible, segmenter );
		new SegmentationAsLabelAction(extensible, segmentationResultsModel, model.labeling());
		MeasureConnectedComponents.addAction(extensible, model);
	}

	private JPanel initLeftPanel()
	{
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new MigLayout("","[grow]","[][][grow]"));
		ActionMap actions = getActions();
		leftPanel.add( trainClassifierButton( actions ), "grow, wrap");
		leftPanel.add(new VisibilityPanel( actions ), "wrap");
		leftPanel.add(new LabelPanel(dialogBoxOwner, new ColoredLabelsModel( model ), fixedLabels).getComponent(), "grow");
		return leftPanel;
	}

	private JButton trainClassifierButton( ActionMap actions )
	{
		JButton button = new JButton( actions.get( "Train Classifier" ) );
		button.setFocusable( false );
		return button;
	}

	private JSplitPane initPanel( JComponent left, JComponent right )
	{
		JSplitPane panel = new JSplitPane();
		panel.setSize(100, 100);
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent( left );
		panel.setRightComponent( right );
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
		segmenter.segment(inputImage.displayImage(), labels);
		return labels;
	}

	public RandomAccessibleInterval<FloatType> getPrediction() {
		RandomAccessibleInterval<FloatType> prediction =
				context.service(OpService.class).create().img(
						RevampUtils.appendDimensionToInterval(inputImage.displayImage(), 0, 1),
						new FloatType());
		segmenter.predict(inputImage.displayImage(), prediction);
		return prediction;
	}

	public boolean isTrained()
	{
		return segmenter.isTrained();
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
		public JFrame dialogParent() {
			return dialogBoxOwner;
		}
	}
}
