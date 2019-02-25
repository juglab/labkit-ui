
package net.imglib2.labkit_trainable_weka;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import ij.ImagePlus;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import trainableSegmentation.WekaSegmentation;

public class TrainableWeka3DLegacyDemo {

	public static void main(String... args) {
		ImagePlus image = new ImagePlus(
			"/home/arzt/Documents/Datasets/Tiny Head/small-head.tif");
		ImagePlus labels = (new ImagePlus(
			"/home/arzt/Documents/Datasets/Tiny Head/small-head.tif.labels.tif"));
		WekaSegmentation wekaSegmentation = new WekaSegmentation(true);
		wekaSegmentation.setClassLabels(new String[] { "background",
			"foreground" });
		subtractOne(labels);
		wekaSegmentation.addLabeledData(image, labels);
		wekaSegmentation.trainClassifier();
		ImagePlus result = wekaSegmentation.applyClassifier(image);
		final Img<? extends RealType<?>> wrap = ImageJFunctions.wrapReal(result);
		BdvFunctions.show(wrap, "title", BdvOptions.options().is2D())
			.setDisplayRange(0, 2);
	}

	private static ImagePlus subtractOne(ImagePlus labels) {
		ImagePlusAdapter.wrapFloat(labels).forEach(x -> x.setReal(x
			.getRealDouble() - 1));
		return labels;
	}
}
