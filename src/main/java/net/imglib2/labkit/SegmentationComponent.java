
package net.imglib2.labkit;

import net.imglib2.labkit.actions.AddLabelingIoAction;
import net.imglib2.labkit.actions.BatchSegmentAction;
import net.imglib2.labkit.actions.BitmapImportExportAction;
import net.imglib2.labkit.actions.ClassifierIoAction;
import net.imglib2.labkit.actions.ClassifierSettingsAction;
import net.imglib2.labkit.actions.LabelEditAction;
import net.imglib2.labkit.actions.LabelingIoAction;
import net.imglib2.labkit.actions.ResetViewAction;
import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.actions.SegmentationExportAction;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.panel.ImageInfoPanel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class SegmentationComponent implements AutoCloseable {

	private final JComponent panel;

	private final boolean unmodifiableLabels;

	private final DefaultExtensible extensible;

	private BasicLabelingComponent labelingComponent;

	private DefaultSegmentationModel segmentationModel;

	public SegmentationComponent(JFrame dialogBoxOwner,
		DefaultSegmentationModel segmentationModel, boolean unmodifiableLabels)
	{
		this.extensible = new DefaultExtensible(segmentationModel.context(),
			dialogBoxOwner);
		this.unmodifiableLabels = unmodifiableLabels;
		this.segmentationModel = segmentationModel;
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner, imageLabelingModel);
		labelingComponent.addBdvLayer(PredictionLayer.createPredictionLayer(segmentationModel));
		initActions();
		this.panel = initPanel();
	}

	private void initActions() {
		final Holder<SegmentationItem> selectedSegmenter = segmentationModel
			.segmenterList().selectedSegmenter();
		final ImageLabelingModel labelingModel = segmentationModel
			.imageLabelingModel();
		new TrainClassifier(extensible, segmentationModel.segmenterList());
		new ClassifierSettingsAction(extensible, selectedSegmenter);
		new ClassifierIoAction(extensible, selectedSegmenter);
		new LabelingIoAction(extensible, labelingModel);
		new AddLabelingIoAction(extensible, labelingModel.labeling());
		new SegmentationExportAction(extensible, labelingModel);
		new ResetViewAction(extensible, labelingModel);
		new BatchSegmentAction(extensible, selectedSegmenter);
		new SegmentationAsLabelAction(extensible, segmentationModel);
		new BitmapImportExportAction(extensible, labelingModel);
		new LabelEditAction(extensible, unmodifiableLabels, new ColoredLabelsModel(
			labelingModel));
		MeasureConnectedComponents.addAction(extensible, labelingModel);
		labelingComponent.addShortcuts(extensible.getShortCuts());
	}

	private JPanel initLeftPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));
		panel.add(ImageInfoPanel.newFramedImageInfoPanel(segmentationModel.imageLabelingModels(),
			labelingComponent), "grow, wrap");
		panel.add(LabelPanel.newFramedLabelPanel(segmentationModel
			.imageLabelingModel(), extensible, unmodifiableLabels), "grow, wrap");
		panel.add(SegmenterPanel.newFramedSegmeterPanel(segmentationModel.segmenterList(),
			extensible), "grow");
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
		return panel;
	}

	public JComponent getComponent() {
		return panel;
	}

	public JMenu createMenu(MenuKey<Void> key) {
		if (key == MenuBar.SEGMENTER_MENU) return extensible.createMenu(
			SegmentationItem.SEGMENTER_MENU, segmentationModel
				.segmenterList().selectedSegmenter()::get);
		return extensible.createMenu(key, () -> null);
	}

	@Override
	public void close() {
		labelingComponent.close();
	}

}
