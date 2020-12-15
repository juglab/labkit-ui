
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
import net.imglib2.labkit.utils.properties.Property;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.panel.ImageInfoPanel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.TrainClassifier;
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

	private BasicLabelingComponent labelingComponent;

	private SegmentationModel segmentationModel;

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
		final Property<SegmentationItem> selectedSegmenter = segmentationModel
			.segmenterList().selectedSegmenter();
		final ImageLabelingModel labelingModel = segmentationModel
			.imageLabelingModel();
		new TrainClassifier(extensible, segmentationModel.segmenterList());
		new ClassifierSettingsAction(extensible, segmentationModel.segmenterList());
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
