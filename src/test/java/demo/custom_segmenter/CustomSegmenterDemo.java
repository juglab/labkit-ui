
package demo.custom_segmenter;

import ij.ImagePlus;
import net.imagej.patcher.LegacyInjector;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.NumericType;
import org.scijava.plugin.Plugin;

/**
 * This class just starts Labkit. Have a look into the {@link CustomSegmenter}
 * and {@link CustomSegmenterPlugin} to learn how to integrate a custom
 * segmentation algorithm into Labkit.
 */
public class CustomSegmenterDemo {

	public static void main(String... args) {
		LegacyInjector.preinit();
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(
			"https://imagej.nih.gov/ij/images/AuPbSn40-2.jpg"));
		LabkitFrame.showForImage(null, new DatasetInputImage(image));
	}

}
