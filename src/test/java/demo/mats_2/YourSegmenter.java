
package demo.mats_2;

import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import org.scijava.Context;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * This class is supposed to show YOU how to implement a segmentation algorithm
 * such that it can be used within the Labkit UI. See {@link MatsDemo2} to run
 * the demo.
 */
class YourSegmenter implements Segmenter {

	private boolean isTrained = false;

	public YourSegmenter(final Context context, final InputImage inputImage) {

	}

	@Override
	public void editSettings(JFrame dialogParent) {

	}

	/**
	 * This method is called, when the user click on train (the little play button
	 * like triangle)
	 *
	 * @param trainingData A list of pairs. Each pair, is a combination of: 1. An
	 *          image to be used for training together. 2. The "drawing" made by the
	 *          user.
	 */
	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {
		System.out.println("MySegmenter.train(...) was called.");

		//// The following lines will show the training data, (if you uncomment
		//// them)

		// for(Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling> pair
		// : data) {
		// ImagePlus originalImage =
		// ImageJFunctions.wrap(Casts.unchecked(pair.getA()), "original image");
		// originalImage.show();
		// ImagePlus drawings =
		// ImageJFunctions.wrap(Casts.unchecked(pair.getB().getIndexImg()),
		// "drawings");
		// drawings.show();
		// }

		isTrained = true;
	}

	/**
	 * This method is supposed to calculate the automatic segmentation.
	 *
	 * @param image The image to be segmented.
	 * @param outputSegmentation The segmentation result needs to be written to this
	 *          image.
	 */

	@Override
	public void segment(ImgPlus<?> image,
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{
		System.out.println("MySegmenter.segment(...) was called.");

		//// The following lines will show the image if you uncomment them

		// ImagePlus originalImage = ImageJFunctions.wrap(Casts.unchecked(image),
		// "Method segment image parameter");
		// originalImage.show();

		drawCircle(outputSegmentation);
	}

	private void drawCircle(
		RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
	{
		Cursor<? extends IntegerType<?>> cursor = Views.iterable(outputSegmentation)
			.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			long x = cursor.getLongPosition(0);
			long y = cursor.getLongPosition(1);
			long radius = square(x - 100) + square(y - 100);
			cursor.get().setInteger(radius < square(50) ? 1 : 0);
		}
	}

	private static long square(long value) {
		return value * value;
	}

	@Override
	public void predict(ImgPlus<?> image,
		RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
	{
		// not needed
	}

	@Override
	public boolean isTrained() {
		return isTrained;
	}

	@Override
	public void saveModel(String path) {

	}

	@Override
	public void openModel(String path) {

	}

	@Override
	public List<String> classNames() {
		return Arrays.asList("background", "foreground");
	}

	@Override
	public int[] suggestCellSize(ImgPlus<?> image) {
		return new int[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
	}
}
