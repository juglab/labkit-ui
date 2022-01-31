
package sc.fiji.labkit.ui.plugin.imaris;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.miginfocom.swing.MigLayout;
import sc.fiji.labkit.ui.BasicLabelingComponent;
import sc.fiji.labkit.ui.DefaultExtensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.SegmentationComponent;
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
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.menu.MenuKey;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.models.SegmenterListModel;
import sc.fiji.labkit.ui.panel.ImageInfoPanel;
import sc.fiji.labkit.ui.panel.LabelPanel;
import sc.fiji.labkit.ui.panel.SegmenterPanel;
import sc.fiji.labkit.ui.plugin.MeasureConnectedComponents;
import sc.fiji.labkit.ui.segmentation.PredictionLayer;
import sc.fiji.labkit.ui.segmentation.TrainClassifier;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import sc.fiji.labkit.ui.utils.ParallelUtils;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;

/**
 * This example is intended on Labkit could be integrated into other tools. An
 * example on how to reuse Labkit components with small modification. This
 * ModifiedSegmentationComponent is very similar to the standard Labkit
 * {@link SegmentationComponent}. Only the "Show results in
 * ImageJ" functionality has been removed. And a button which does pretty much
 * the same has been added es exmaple.
 */
public class ImarisSegmentationComponent extends JPanel implements AutoCloseable {

	private final boolean unmodifiableLabels = false;

	private final DefaultExtensible extensible;

	private final BasicLabelingComponent labelingComponent;

	private final SegmentationModel segmentationModel;

	private final ImarisExtensionPoints extensionPoints;

	private final boolean closeLabkitAfterCalculatingResult;

	public ImarisSegmentationComponent(
			final JFrame dialogBoxOwner,
			final SegmentationModel segmentationModel,
			final ImarisExtensionPoints extensionPoints,
			final boolean closeLabkitAfterCalculatingResult)
	{
		this.extensionPoints = extensionPoints;
		this.closeLabkitAfterCalculatingResult = closeLabkitAfterCalculatingResult;
		this.extensible = new DefaultExtensible(segmentationModel.context(),
			dialogBoxOwner);
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
		final ImageLabelingModel labelingModel = segmentationModel.imageLabelingModel();
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
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow][]"));
		panel.add(ImageInfoPanel.newFramedImageInfoPanel(segmentationModel.imageLabelingModel(),
			labelingComponent), "grow, wrap");
		panel.add(LabelPanel.newFramedLabelPanel(segmentationModel
			.imageLabelingModel(), extensible, unmodifiableLabels), "grow, wrap, height 0:50");
		panel.add(SegmenterPanel.newFramedSegmeterPanel(segmentationModel.segmenterList(),
			extensible), "grow, height 0:50, wrap");
		panel.add(initSaveResultsButton(), "grow, wrap");
		panel.invalidate();
		panel.repaint();
		return panel;
	}

	private JButton initSaveResultsButton() {
		final JButton button = new JButton("Compute result and send it to Imaris" );
		button.addActionListener( e -> onSaveResultsClicked() );
		return button;
	}

	private void onSaveResultsClicked() {
		final SegmentationItem selectedSegmenter = segmentationModel.segmenterList().selectedSegmenter().get();
		if ( selectedSegmenter == null || !selectedSegmenter.isTrained() )
		{
			JOptionPane.showMessageDialog( null, "Please select a segmentation algorithm and train it" );
			return;
		}

		final ImageLabelingModel imageLabeling = segmentationModel.imageLabelingModel();
		final RandomAccessibleInterval<FloatType> prediction = selectedSegmenter.results(imageLabeling).prediction();
		ParallelUtils.runInOtherThread( () -> {
			final SwingProgressWriter progress = new SwingProgressWriter(null, "Calculate Entire Segmentation");
			ParallelUtils.populateCachedImg(prediction, progress);
			extensionPoints.setImarisImage(prediction);

			SwingUtilities.invokeLater(() -> {
				// hide progress bar
				progress.setVisible(false);

				if (closeLabkitAfterCalculatingResult) {
					// Close Labkit window
					final JFrame frame = extensible.dialogParent();
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
			});
		} );

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

	public void loadClassifier(final String classifier, final boolean useGpu) {
		final SegmenterListModel segmenterList = segmentationModel.segmenterList();
		final SegmentationItem item = segmenterList.addSegmenter(PixelClassificationPlugin.create(useGpu));
		item.openModel(classifier);

		final Labeling labeling = segmentationModel.imageLabelingModel().labeling().get();
		new ArrayList<>(labeling.getLabels()).forEach(labeling::removeLabel);
		item.classNames().forEach(labeling::addLabel);
		segmentationModel.imageLabelingModel().labeling().notifier().notifyListeners();

		segmenterList.selectedSegmenter().set(item);
	}
}
