/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package demo;

import bdv.util.BdvFunctions;
import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import sc.fiji.labkit.ui.BasicLabelingComponent;
import sc.fiji.labkit.ui.DefaultExtensible;
import sc.fiji.labkit.ui.MenuBar;
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
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.menu.MenuKey;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
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
import sc.fiji.labkit.ui.utils.ParallelUtils;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;
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
 * This example is intended to show haw Labkit could be integrated into other
 * tools. An example on how to reuse Labkit components with small modification.
 * This CustomizedSegmentationComponent is very similar to the standard Labkit
 * {@link sc.fiji.labkit.ui.SegmentationComponent}. Only the "Show results in
 * ImageJ" functionality has been removed. And a button which shows the result
 * in Big Data Viewer has been added.
 */
public class CustomizedSegmentationComponentDemo extends JPanel implements AutoCloseable {

	private final DefaultExtensible extensible;

	private final BasicLabelingComponent labelingComponent;

	private final SegmentationModel segmentationModel;

	public CustomizedSegmentationComponentDemo(JFrame dialogBoxOwner,
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
		new ClassifierIoAction(extensible, segmentationModel.segmenterList());
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
		JButton button = new JButton("Show results in BDV");
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
		RandomAccessibleInterval<UnsignedByteType> segmentation =
			selectedSegmenter.results(imageLabeling).segmentation();
		ParallelUtils.runInOtherThread(() -> {
			ParallelUtils.populateCachedImg(segmentation, new SwingProgressWriter(null,
				"Calculate Entire Segmentation"));
			JOptionPane.showMessageDialog(null, "Calculation completed");
			BdvFunctions.show(segmentation, "Segmentation").setDisplayRange(0, 1);
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
		if (key == sc.fiji.labkit.ui.MenuBar.SEGMENTER_MENU)
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
		JFrame frame = new JFrame("Labkit UI Customization Demo");
		ImgPlus<?> image = VirtualStackAdapter.wrap(new ImagePlus(
			"https://imagej.nih.gov/ij/images/t1-head.zip"));
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(new Context(),
			new DatasetInputImage(image));
		CustomizedSegmentationComponentDemo component = new CustomizedSegmentationComponentDemo(frame,
			segmentationModel);
		component.autoContrast();
		frame.setJMenuBar(component.getMenuBar());
		frame.add(component);
		frame.setSize(800, 500);
		frame.setVisible(true);
	}
}
