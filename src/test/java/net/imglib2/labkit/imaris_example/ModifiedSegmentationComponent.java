
package net.imglib2.labkit.imaris_example;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.BasicLabelingComponent;
import net.imglib2.labkit.DefaultExtensible;
import net.imglib2.labkit.MenuBar;
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
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationModel;
import net.imglib2.labkit.panel.ImageInfoPanel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.type.numeric.integer.ShortType;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

/**
 * This example is intended on Labkit could be integrated into other tools. An
 * example on how to reuse Labkit components with small modification. This
 * ModifiedSegmentationComponent is very similar to the standard Labkit
 * {@link net.imglib2.labkit.SegmentationComponent}. Only the "Show results in
 * ImageJ" functionality has been removed. And a button which does pretty much
 * the same has been added es exmaple.
 */
public class ModifiedSegmentationComponent extends JPanel implements AutoCloseable {

	private final DefaultExtensible extensible;

	private final BasicLabelingComponent labelingComponent;

	private final SegmentationModel segmentationModel;

	public ModifiedSegmentationComponent(JFrame dialogBoxOwner,
		SegmentationModel segmentationModel)
	{
		this.extensible = new DefaultExtensible(segmentationModel.context(), dialogBoxOwner);
		this.segmentationModel = segmentationModel;
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner, imageLabelingModel);
		labelingComponent.addBdvLayer(PredictionLayer.createPredictionLayer(segmentationModel));
		initActions();
		setLayout(new BorderLayout());
		add(initGui());
	}

	private void initActions() {
		final Holder<SegmentationItem> selectedSegmenter = segmentationModel.segmenterList()
			.selectedSegmenter();
		final ImageLabelingModel labelingModel = segmentationModel.imageLabelingModel();
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
		new LabelEditAction(extensible, false, new ColoredLabelsModel(labelingModel));
		MeasureConnectedComponents.addAction(extensible, labelingModel);
		labelingComponent.addShortcuts(extensible.getShortCuts());
	}

	private JPanel initLeftPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow][]"));
		panel.add(ImageInfoPanel.newFramedImageInfoPanel(segmentationModel.imageLabelingModel(),
			labelingComponent), "grow, wrap");
		panel.add(LabelPanel.newFramedLabelPanel(segmentationModel
			.imageLabelingModel(), extensible, false), "grow, wrap, height 0:50");
		panel.add(SegmenterPanel.newFramedSegmeterPanel(segmentationModel.segmenterList(),
			extensible), "grow, height 0:50, wrap");
		panel.add(initSaveResultsButton());
		panel.invalidate();
		panel.repaint();
		return panel;
	}

	private JButton initSaveResultsButton() {
		JButton button = new JButton("Save results");
		button.addActionListener(this::onSaveResultsClicked);
		return button;
	}

	private void onSaveResultsClicked(ActionEvent actionEvent) {
		SegmentationItem selectedSegmenter = segmentationModel.segmenterList().selectedSegmenter()
			.get();
		if (selectedSegmenter == null || !selectedSegmenter.isTrained()) {
			JOptionPane.showMessageDialog(null, "Please select a segmentation algoritm and train it");
			return;
		}
		ImageLabelingModel imageLabeling = segmentationModel.imageLabelingModel();
		RandomAccessibleInterval<ShortType> segmentation = selectedSegmenter.results(imageLabeling)
			.segmentation();
		ParallelUtils.runInOtherThread(() -> {
			ParallelUtils.populateCachedImg(segmentation, new SwingProgressWriter(null,
				"Calculate Entire Segmentation"));
			JOptionPane.showMessageDialog(null, "Calculation completed");
			ImageJFunctions.show(segmentation, "Segmentation");
		});
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
		if (key == net.imglib2.labkit.MenuBar.SEGMENTER_MENU)
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

	static {
		LegacyInjector.preinit();
	}

	public static void main(String... args) {
		JFrame frame = new JFrame("Labkit Imaris Demo");
		ImgPlus<?> image = VirtualStackAdapter.wrap(new ImagePlus(
			"https://imagej.nih.gov/ij/images/t1-head.zip"));
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(new Context(),
			new DatasetInputImage(image));
		ModifiedSegmentationComponent component = new ModifiedSegmentationComponent(frame,
			segmentationModel);
		component.autoContrast();
		frame.setJMenuBar(component.getMenuBar());
		frame.add(component);
		frame.setSize(800, 500);
		frame.setVisible(true);
	}
}
