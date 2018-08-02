
package net.imglib2.labkit;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
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
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.panel.GuiUtils;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.segmentation.weka.TimeSeriesSegmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class SegmentationComponent implements AutoCloseable {

	private final JComponent panel;

	private final boolean fixedLabels;

	private final JFrame dialogBoxOwner;

	private BasicLabelingComponent labelingComponent;

	private final Context context;

	private final InputImage inputImage;

	private DefaultSegmentationModel segmentationModel;

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
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner,
			segmentationModel.imageLabelingModel());
		labelingComponent.addBdvLayer(new PredictionLayer(segmentationModel
			.selectedSegmenter()));
		initActions();
		this.panel = initPanel();
	}

	private void initModels(InputImage image, Labeling labeling) {
		segmentationModel = new DefaultSegmentationModel(image,
			() -> initClassifier(context));
		segmentationModel.imageLabelingModel().labeling().set(labeling);
	}

	private Segmenter initClassifier(Context context) {
		// FIXME: should this be placed in SegmentationModel
		TrainableSegmentationSegmenter classifier1 =
			new TrainableSegmentationSegmenter(context, inputImage);
		return inputImage.isTimeSeries() ? new TimeSeriesSegmenter(classifier1)
			: classifier1;
	}

	private void initActions() {
		DefaultExtensible extensible = new DefaultExtensible(context,
			dialogBoxOwner, labelingComponent);
		new TrainClassifier(extensible, segmentationModel);
		new ClassifierIoAction(extensible, segmentationModel.selectedSegmenter());
		new LabelingIoAction(extensible, segmentationModel.imageLabelingModel()
			.labeling(), inputImage);
		new AddLabelingIoAction(extensible, segmentationModel.imageLabelingModel()
			.labeling());
		new SegmentationSave(extensible, segmentationModel.selectedSegmenter());
		new OpenImageAction(extensible);
		new OrthogonalView(extensible, segmentationModel.imageLabelingModel());
		new SelectClassifier(extensible, segmentationModel.selectedSegmenter());
		new BatchSegmentAction(extensible, segmentationModel.selectedSegmenter());
		new SegmentationAsLabelAction(extensible, segmentationModel
			.selectedSegmenter(), segmentationModel.imageLabelingModel().labeling());
		new BitmapImportExportAction(extensible, segmentationModel
			.imageLabelingModel());
		MeasureConnectedComponents.addAction(extensible, segmentationModel
			.imageLabelingModel());
	}

	private JPanel initLeftPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));
		ActionMap actions = getActions();
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Image"), GuiUtils
			.createDimensionsInfo(inputImage.interval())), "grow, wrap");
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Labeling"),
			new LabelPanel(dialogBoxOwner, new ColoredLabelsModel(segmentationModel
				.imageLabelingModel()), fixedLabels).getComponent()), "grow, wrap");
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Segmentation"),
			new SegmenterPanel(segmentationModel).getComponent()), "grow");
		panel.invalidate();
		panel.repaint();
		return panel;
	}

	private JComponent initPanel() {
		JSplitPane panel = new JSplitPane();
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(initLeftPanel());
		panel.setRightComponent(labelingComponent.getComponent());
		panel.setBorder(BorderFactory.createEmptyBorder());
		// panel.add( bottom );
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

}
