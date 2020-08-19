
package net.imglib2.labkit.project;

import net.imglib2.trainable_segmentation.utils.SingletonContext;
import org.scijava.Context;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates {@link LabeledImagesListPanel}.
 */
public class LabeledImagesListPanelDemo {

	public static void main(String... args) {
		Context context = SingletonContext.getInstance();
		List<LabeledImage> files = Stream.of(
			"/home/arzt/tmp/labkit-project/phase1.tif",
			"/home/arzt/tmp/labkit-project/phase2.tif",
			"/home/arzt/tmp/labkit-project/phase3.tif",
			"/home/arzt/tmp/labkit-project/phase4.tif")
			.map(filename -> new LabeledImage(SingletonContext.getInstance(), filename))
			.collect(Collectors.toList());
		LabkitProjectModel model = new LabkitProjectModel(context, "/home/arzt/tmp/labkit-project",
			files);
		LabkitProjectEditor.show(model);
	}
}
