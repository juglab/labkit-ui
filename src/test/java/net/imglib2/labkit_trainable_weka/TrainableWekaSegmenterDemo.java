
package net.imglib2.labkit_trainable_weka;

import ij.ImagePlus;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.type.numeric.NumericType;
import org.scijava.Context;

public class TrainableWekaSegmenterDemo {

	public static void main(String... args) {
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(
			"https://imagej.nih.gov/ij/images/AuPbSn40-2.jpg"));
		Context context = new Context();
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(
			new DefaultInputImage(image), context, TrainableWekaSegmenter::new);
		LabkitFrame.show(segmentationModel, "Demonstrate other Segmenter");
	}

}
