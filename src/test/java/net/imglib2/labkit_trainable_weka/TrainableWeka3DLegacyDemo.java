
package net.imglib2.labkit_trainable_weka;

import ij.ImagePlus;
import ij.ImageStack;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import trainableSegmentation.WekaSegmentation;

import java.util.Arrays;

public class TrainableWeka3DLegacyDemo {

	public static void main(String... args) {
		// setup images
		ImagePlus image = asZStack(new byte[] { 1, 0, 0, 1, 1, 0, 0, 1 });
		ImagePlus labels = asZStack(new byte[] { 1, 0, -1, -1, -1, -1, -1, -1 });
		// train classifier
		WekaSegmentation wekaSegmentation = new WekaSegmentation(true);
		wekaSegmentation.addLabeledData(image, labels);
		wekaSegmentation.trainClassifier();
		// apply classifier
		ImagePlus result = wekaSegmentation.applyClassifier(image);
		// show result
		// result.show();
		System.out.println("Segmentation: " + Arrays.toString(getPixels(result)));
	}

	private static ImagePlus asZStack(byte[] imagePixels) {
		// NB: This is hard coded to a image size of 2x2x2 pixels.
		ImagePlus image = ImageJFunctions.wrap(ArrayImgs.unsignedBytes(imagePixels,
			2, 2, 2), "title");
		image.setStack(image.getStack(), 1, 2, 1);
		return image;
	}

	private static byte[] getPixels(ImagePlus imagePlus) {
		final ImageStack stack = imagePlus.getStack();
		int planeSize = stack.getWidth() * stack.getHeight();
		byte[] pixels = new byte[planeSize * stack.getSize()];
		for (int i = 0; i < stack.getSize(); i++) {
			System.arraycopy(stack.getPixels(i + 1), 0, pixels, i * planeSize,
				planeSize);
		}
		return pixels;
	}
}
