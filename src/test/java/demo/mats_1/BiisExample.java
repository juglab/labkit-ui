
package demo.mats_1;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.type.numeric.NumericType;
import org.scijava.Context;

/**
 * This shows
 */
public class BiisExample {

	private static final String example1 =
		"https://cgcweb.med.tu-dresden.de/cloud/index.php/s/G2ML3C6yYtnZW82/download?path=%2F&files=original1.tif";
	private static final String example2 =
		"https://cgcweb.med.tu-dresden.de/cloud/index.php/s/G2ML3C6yYtnZW82/download?path=%2F&files=original2.tif";
	private static final String examples3 =
		"https://cgcweb.med.tu-dresden.de/cloud/index.php/s/G2ML3C6yYtnZW82/download?path=%2Fbsp3&files=nativ.tif";

	public static void main(String... args) {
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(example2));
		Context context = new Context();
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(
			new DatasetInputImage(image), context);
		LabkitFrame.show(segmentationModel, "Demonstrate other Segmenter");
	}
}
