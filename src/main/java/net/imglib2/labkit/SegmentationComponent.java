
package net.imglib2.labkit;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.actions.AddLabelingIoAction;
import net.imglib2.labkit.actions.BatchSegmentAction;
import net.imglib2.labkit.actions.BitmapImportExportAction;
import net.imglib2.labkit.actions.ClassifierIoAction;
import net.imglib2.labkit.actions.LabelingIoAction;
import net.imglib2.labkit.actions.ResetViewAction;
import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.actions.SegmentationSave;
import net.imglib2.labkit.actions.ClassifierSettingsAction;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.panel.GuiUtils;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.*;
import java.util.List;

public class SegmentationComponent implements AutoCloseable {

	private final JComponent panel;

	private final boolean fixedLabels;

	private final JFrame dialogBoxOwner;

	private BasicLabelingComponent labelingComponent;

	private final Context context;

	private DefaultSegmentationModel segmentationModel;

	private SegmentationAsLabelAction sal;

	public SegmentationComponent(Context context, JFrame dialogBoxOwner,
		DefaultSegmentationModel segmentationModel, boolean fixedLabels)
	{
		this.dialogBoxOwner = dialogBoxOwner;
		this.context = context;
		this.fixedLabels = fixedLabels;
		this.segmentationModel = segmentationModel;
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner,
			segmentationModel.imageLabelingModel());
		labelingComponent.addBdvLayer(new PredictionLayer(segmentationModel
			.selectedSegmenter()));
		initActions();
		this.panel = initPanel();
	}

	private void initActions() {
		DefaultExtensible extensible = new DefaultExtensible(context,
			dialogBoxOwner, labelingComponent);
		new TrainClassifier(extensible, segmentationModel);
		new ClassifierIoAction(extensible, segmentationModel.selectedSegmenter());
		new LabelingIoAction(extensible, segmentationModel.imageLabelingModel());
		new AddLabelingIoAction(extensible, segmentationModel.imageLabelingModel()
			.labeling());
		new SegmentationSave(extensible, segmentationModel.selectedSegmenter());
		new ResetViewAction(extensible, segmentationModel.imageLabelingModel());
		new ClassifierSettingsAction(extensible, segmentationModel
			.selectedSegmenter());
		new BatchSegmentAction(extensible, segmentationModel.selectedSegmenter());
		sal = new SegmentationAsLabelAction(extensible, segmentationModel
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
			.createDimensionsInfo(segmentationModel.image())), "grow, wrap");
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Labeling"),
			new LabelPanel(dialogBoxOwner, new ColoredLabelsModel(segmentationModel
				.imageLabelingModel()), fixedLabels, actions).getComponent()),
			"grow, wrap");
		panel.add(GuiUtils.createCheckboxGroupedPanel(actions.get("Segmentation"),
			new SegmenterPanel(segmentationModel, sal).getComponent()), "grow");
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
