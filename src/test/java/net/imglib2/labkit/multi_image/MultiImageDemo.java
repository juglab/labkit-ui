
package net.imglib2.labkit.multi_image;

import net.imagej.patcher.LegacyInjector;
import net.imglib2.labkit.SegmentationComponent;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.LabeledImage;
import net.imglib2.labkit.models.LabkitProjectModel;
import net.imglib2.trainable_segmentation.utils.SingletonContext;

import javax.swing.*;
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
		DefaultSegmentationModel segmentationModel = ProjectSegmentationModel.init(labkitProjectModel);
		SegmentationComponent component = new SegmentationComponent(frame, segmentationModel, false);
		component.autoContrast();
		frame.setJMenuBar(component.getMenuBar());
		JPanel panel = new LabkitProjectView(labkitProjectModel);
		labkitProjectModel.selectedImage().notifier().add(component::autoContrast);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, component.getComponent(),
			panel);
		splitPane.setResizeWeight(1);
		splitPane.setOneTouchExpandable(true);
		frame.add(splitPane);
		frame.pack();
		frame.setVisible(true);
	}
}
