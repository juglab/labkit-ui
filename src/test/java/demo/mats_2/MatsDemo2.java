
package demo.mats_2;

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
public class MatsDemo2 {

	public static void main(String... args) {
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(
			"/home/arzt/Desktop/original1.tif"));
		Context context = new Context();
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(
			new DatasetInputImage(image), context, YourSegmenter::new);
		LabkitFrame.show(segmentationModel, "Demonstrate other Segmenter");
	}
}
