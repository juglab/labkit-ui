
package net.imglib2.labkit_trainable_weka;

import ij.IJ;
import ij.ImagePlus;
import net.imglib2.img.ImagePlusAdapter;
import trainableSegmentation.WekaSegmentation;

public class TrainableWeka2DLegacyDemo {

	public static void main(String... args) {
		ImagePlus image = new ImagePlus(
			"https://imagej.nih.gov/ij/images/AuPbSn40-2.jpg");
		ImagePlus labels = new ImagePlus("/home/arzt/AuPbSn40.jpg.labels.tif");
		WekaSegmentation wekaSegmentation = new WekaSegmentation();
		subtractOne(labels);
		wekaSegmentation.addLabeledData(image, labels);
		wekaSegmentation.trainClassifier();
		ImagePlus result = wekaSegmentation.applyClassifier(image);
		IJ.run(result, "Multiply...", "value=255 stack");
		result.show();
	}

	private static void subtractOne(ImagePlus labels) {
		ImagePlusAdapter.wrapFloat(labels).forEach(x -> x.setReal(x
			.getRealDouble() - 1));
	}
}
