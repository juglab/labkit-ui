
package sc.fiji.labkit.ui;

import sc.fiji.labkit.ui.actions.AddLabelingIoAction;
import sc.fiji.labkit.ui.actions.BatchSegmentAction;
import sc.fiji.labkit.ui.actions.BitmapImportExportAction;
import sc.fiji.labkit.ui.actions.ClassifierIoAction;
import sc.fiji.labkit.ui.actions.ClassifierSettingsAction;
import sc.fiji.labkit.ui.actions.LabelEditAction;
import sc.fiji.labkit.ui.actions.LabelingIoAction;
import sc.fiji.labkit.ui.actions.ResetViewAction;
import sc.fiji.labkit.ui.actions.SegmentationAsLabelAction;
import sc.fiji.labkit.ui.actions.SegmentationExportAction;
import sc.fiji.labkit.ui.menu.MenuKey;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.panel.ImageInfoPanel;
import sc.fiji.labkit.ui.panel.LabelPanel;
import sc.fiji.labkit.ui.panel.SegmenterPanel;
import sc.fiji.labkit.ui.plugin.MeasureConnectedComponents;
import sc.fiji.labkit.ui.segmentation.PredictionLayer;
import sc.fiji.labkit.ui.segmentation.TrainClassifier;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * {@link SegmentationComponent} is the central Labkit UI component. Provides UI
 * to display and modify a {@link SegmentationModel}.
 * <p>
 * The UI consist of a Big Data Viewer panel, with brush tools and a side bar.
 * The side bar lists panels and segmentation algorithms.
 * <p>
 * A main menu that contains many actions for open and saving data, can be
 * accessed by using {@link #getMenuBar()}.
 */
public class SegmentationComponent extends JPanel implements AutoCloseable {

	private final boolean unmodifiableLabels;

	private final DefaultExtensible extensible;

	private final BasicLabelingComponent labelingComponent;

	private final SegmentationModel segmentationModel;

	public SegmentationComponent(JFrame dialogBoxOwner,
		SegmentationModel segmentationModel, boolean unmodifiableLabels)
	{
		this.extensible = new DefaultExtensible(segmentationModel.context(),
			dialogBoxOwner);
		this.unmodifiableLabels = unmodifiableLabels;
		this.segmentationModel = segmentationModel;
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner, imageLabelingModel);
		labelingComponent.addBdvLayer(PredictionLayer.createPredictionLayer(segmentationModel));
		initActions();
		setLayout(new BorderLayout());
		add(initGui());
	}

	private void initActions() {
		final Holder<SegmentationItem> selectedSegmenter = segmentationModel
			.segmenterList().selectedSegmenter();
		final ImageLabelingModel labelingModel = segmentationModel
			.imageLabelingModel();
		new TrainClassifier(extensible, segmentationModel.segmenterList());
		new ClassifierSettingsAction(extensible, segmentationModel.segmenterList());
		new ClassifierIoAction(extensible, segmentationModel.segmenterList());
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
		panel.add(ImageInfoPanel.newFramedImageInfoPanel(segmentationModel.imageLabelingModel(),
			labelingComponent), "grow, wrap");
		panel.add(LabelPanel.newFramedLabelPanel(segmentationModel
			.imageLabelingModel(), extensible, unmodifiableLabels), "grow, wrap, height 0:50");
		panel.add(SegmenterPanel.newFramedSegmeterPanel(segmentationModel.segmenterList(),
			extensible), "grow, height 0:50");
		panel.invalidate();
		panel.repaint();
		return panel;
	}

	private JSplitPane initGui() {
		JSplitPane panel = new JSplitPane();
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(initLeftPanel());
		panel.setRightComponent(labelingComponent);
		panel.setBorder(BorderFactory.createEmptyBorder());
		return panel;
	}

	@Deprecated
	public JComponent getComponent() {
		return this;
	}

	public JMenu createMenu(MenuKey<Void> key) {
		if (key == MenuBar.SEGMENTER_MENU)
			return extensible.createMenu(
				SegmentationItem.SEGMENTER_MENU, segmentationModel
					.segmenterList().selectedSegmenter()::get);
		return extensible.createMenu(key, () -> null);
	}

	@Override
	public void close() {
		labelingComponent.close();
	}

	public JMenuBar getMenuBar() {
		return new MenuBar(this::createMenu);
	}

	public void autoContrast() {
		labelingComponent.autoContrast();
	}
}
