
package net.imglib2.labkit.multi_image;

import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.trainable_segmentation.utils.SingletonContext;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiImageDemo {

	static {
		LegacyInjector.preinit();
	}

	private static List<LabeledImage> files = Stream.of(
		"/home/arzt/tmp/labkit-project/phase1.tif",
		"/home/arzt/tmp/labkit-project/phase2.tif",
		"/home/arzt/tmp/labkit-project/phase3.tif",
		"/home/arzt/tmp/labkit-project/phase4.tif")
		.map(LabeledImage::new)
		.collect(Collectors.toList());

	public static void main(String... args) {
		List<LabeledImage> imageFiles = files;
		LabkitProjectModel labkitProjectModel = new LabkitProjectModel(
			SingletonContext.getInstance(), imageFiles);
		JFrame frame = new JFrame("Labkit Project");
		SegmentationComponent component = new SegmentationComponent(frame, labkitProjectModel
			.segmentationModel(), false);
		JList<LabeledImage> list = initList(labkitProjectModel, component);
		component.autoContrast();
		frame.setJMenuBar(component.getMenuBar());
		frame.add(component.getComponent());
		frame.add(new JScrollPane(list), BorderLayout.LINE_END);
		frame.pack();
		frame.setVisible(true);
	}

	private static JList<LabeledImage> initList(LabkitProjectModel labkitProjectModel,
		SegmentationComponent component)
	{
		JList<LabeledImage> list = new JList<>(labkitProjectModel.labeledImages().toArray(
			new LabeledImage[0]));
		list.addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting()) {
				labkitProjectModel.selectLabeledImage(list.getSelectedValue());
				component.autoContrast();
			}
		});
		return list;
	}
}
