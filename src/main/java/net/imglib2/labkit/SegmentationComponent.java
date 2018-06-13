
package net.imglib2.labkit;

import net.imagej.ops.OpService;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.labkit.actions.AddLabelingIoAction;
import net.imglib2.labkit.actions.BatchSegmentAction;
import net.imglib2.labkit.actions.BitmapImportExportAction;
import net.imglib2.labkit.actions.ClassifierIoAction;
import net.imglib2.labkit.actions.LabelingIoAction;
import net.imglib2.labkit.actions.OpenImageAction;
import net.imglib2.labkit.actions.OrthogonalView;
import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.actions.SegmentationSave;
import net.imglib2.labkit.actions.SelectClassifier;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.panel.VisibilityPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.segmentation.weka.TimeSeriesSegmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.labkit.utils.ProgressConsumer;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SegmentationComponent implements AutoCloseable {

	private final JSplitPane panel;

	private final boolean fixedLabels;

	private final JFrame dialogBoxOwner;

	private LabelingComponent labelingComponent;

	private ImageLabelingModel model;

	private final Context context;

	private final InputImage inputImage;

	private SegmentationModel segmentationModel;

	public SegmentationComponent(Context context, JFrame dialogBoxOwner,
		RandomAccessibleInterval<? extends NumericType<?>> image,
		boolean isTimeSeries)
	{
		this(context, dialogBoxOwner, initInputImage(image, isTimeSeries),
			defaultLabeling(image), true);
	}

	private static Labeling defaultLabeling(Interval image) {
		return new Labeling(Arrays.asList("background", "foreground"), image);
	}

	public SegmentationComponent(Context context, JFrame dialogBoxOwner,
		InputImage image)
	{
		this(context, dialogBoxOwner, image, defaultLabeling(image.interval()),
			false);
	}

	private static DefaultInputImage initInputImage(
		RandomAccessibleInterval<? extends NumericType<?>> image,
		boolean isTimeSeries)
	{
		DefaultInputImage defaultInputImage = new DefaultInputImage(image);
		defaultInputImage.setTimeSeries(isTimeSeries);
		return defaultInputImage;
	}

	public SegmentationComponent(Context context, JFrame dialogBoxOwner,
		InputImage image, Labeling labeling, boolean fixedLabels)
	{
		this.dialogBoxOwner = dialogBoxOwner;
		this.inputImage = image;
		this.context = context;
		this.fixedLabels = fixedLabels;
		initModels(image, labeling);
		labelingComponent = new LabelingComponent(dialogBoxOwner, model);
		labelingComponent.addBdvLayer(new PredictionLayer(segmentationModel
			.selectedSegmenter()));
		initActions();
		JPanel leftPanel = initLeftPanel();
		this.panel = initPanel(leftPanel, labelingComponent.getComponent());
	}

	private void initModels(InputImage image, Labeling labeling) {
		model = new ImageLabelingModel(image.showable(), labeling, inputImage
			.isTimeSeries());
		segmentationModel = new SegmentationModel(image.imageForSegmentation(),
			model, () -> initClassifier(context));
	}

	private Segmenter initClassifier(Context context) {
		// FIXME: should this be placed in SegmentationModel
		TrainableSegmentationSegmenter classifier1 =
			new TrainableSegmentationSegmenter(context, inputImage);
		return inputImage.isTimeSeries() ? new TimeSeriesSegmenter(classifier1)
			: classifier1;
	}

	private void initActions() {
		MyExtensible extensible = new MyExtensible();
		new TrainClassifier(extensible, segmentationModel);
		new ClassifierIoAction(extensible, segmentationModel.selectedSegmenter());
		new LabelingIoAction(extensible, model.labeling(), inputImage);
		new AddLabelingIoAction(extensible, model.labeling());
		new SegmentationSave(extensible, segmentationModel.selectedSegmenter());
		new OpenImageAction(extensible);
		new OrthogonalView(extensible, model);
		new SelectClassifier(extensible, segmentationModel.selectedSegmenter());
		new BatchSegmentAction(extensible, segmentationModel.selectedSegmenter());
		new SegmentationAsLabelAction(extensible, segmentationModel
			.selectedSegmenter(), model.labeling());
		new BitmapImportExportAction(extensible, model);
		MeasureConnectedComponents.addAction(extensible, model);
	}

	private JPanel initLeftPanel() {
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		ActionMap actions = getActions();
		leftPanel.add(new VisibilityPanel(actions), "wrap");
		leftPanel.add(new LabelPanel(dialogBoxOwner, new ColoredLabelsModel(model),
			fixedLabels).getComponent(), "grow, wrap");
		leftPanel.add(new SegmenterPanel(segmentationModel, actions).getComponent(),
			"grow");
		return leftPanel;
	}

	private JSplitPane initPanel(JComponent left, JComponent right) {
		JSplitPane panel = new JSplitPane();
		panel.setSize(100, 100);
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(left);
		panel.setRightComponent(right);
		return panel;
	}

	public JComponent getComponent() {
		return panel;
	}

	public ActionMap getActions() {
		return labelingComponent.getActions();
	}

	public <T extends IntegerType<T> & NativeType<T>>
		List<RandomAccessibleInterval<T>> getSegmentations(T type)
	{
		return segmentationModel.getSegmentations(type);
	}

	public List<RandomAccessibleInterval<FloatType>> getPredictions() {
		return segmentationModel.getPredictions();
	}

	public boolean isTrained() {
		return segmentationModel.isTrained();
	}

	@Override
	public void close() {
		labelingComponent.close();
	}

	private class MyExtensible implements Extensible {

		@Override
		public Context context() {
			return context;
		}

		@Override
		public void addAction(String title, String command, Runnable action,
			String keyStroke)
		{
			RunnableAction a = new RunnableAction(title, action);
			a.putValue(Action.ACTION_COMMAND_KEY, command);
			a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
			labelingComponent.addAction(a);
		}

		@Override
		public JFrame dialogParent() {
			return dialogBoxOwner;
		}

		@Override
		public ProgressConsumer progressConsumer() {
			return context.getService(StatusService.class)::showProgress;
		}
	}
}
