
package demo.mats_1;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Casts;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import org.scijava.Context;
import preview.net.imglib2.loops.LoopBuilder;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * This class is supposed to show YOU how to implement a segmentation algorithm
 * such that it can be used within the Labkit UI. See {@link BiisExample} to run
 * the demo.
 */
class BiisSegmenterPlugin implements Segmenter {

	private final BiisSegmenter biisSegmenter = new BiisSegmenter();
	private boolean isTrained = false;

	public BiisSegmenterPlugin(final Context context, final InputImage inputImage) {

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
		Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling> pair = trainingData.get(0);
		ImagePlus originalImage = ImageJFunctions.wrap(Casts.unchecked(pair.getA()), "original image");
		Labeling b = pair.getB();
		ImagePlus drawings = ImageJFunctions.wrap(b.getRegion(b.getLabel("background")), "drawings");
		drawings.setDisplayRange(0, 1);
		biisSegmenter.train(originalImage, drawings);
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
		ImagePlus originalImage = ImageJFunctions.wrap(Casts.unchecked(image), "original image");
		ImagePlus segmentation = biisSegmenter.apply(originalImage);
		Img<? extends RealType<?>> result = ImageJFunctions.wrapReal(segmentation);
		LoopBuilder.setImages(result, outputSegmentation).forEachPixel((i, o) -> o.setInteger(i
			.getRealDouble() < 1 ? 0 : 1));
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
