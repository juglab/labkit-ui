
package net.imglib2.labkit;

import ij.ImagePlus;
import io.scif.img.ImgSaver;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.utils.ParallelUtils;
import bdv.export.ProgressWriter;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Helper class for segmenting multiple image files.
 *
 * @author Matthias Arzt
 */
public class BatchSegmenter {

	private final ImgSaver saver = new ImgSaver();
	private final Segmenter segmenter;
	private final ProgressWriter progressWriter;

	public BatchSegmenter(Segmenter segmenter, ProgressWriter progressWriter) {
		this.segmenter = segmenter;
		this.progressWriter = progressWriter;
	}

	public void segment(File inputFile, File outputFile) throws Exception {
		ImgPlus<?> img = VirtualStackAdapter.wrap(new ImagePlus(inputFile
			.getAbsolutePath()));
		Img<UnsignedByteType> segmentation = segment(img, segmenter, Intervals
			.dimensionsAsIntArray(img), progressWriter);
		saver.saveImg(outputFile.getAbsolutePath(), segmentation);
	}

	public static Img<UnsignedByteType> segment(ImgPlus<?> rawImg,
		Segmenter segmenter, int[] cellDimensions, ProgressWriter progressWriter)
		throws InterruptedException
	{
		Consumer<RandomAccessibleInterval<UnsignedByteType>> loader =
			target -> segmenter.segment(rawImg, target);
		Img<UnsignedByteType> result = ArrayImgs.unsignedBytes(Intervals
			.dimensionsAsLongArray(rawImg));
		List<Callable<Void>> chunks = ParallelUtils.chunkOperation(result,
			cellDimensions, loader);
		ParallelUtils.executeInParallel(Executors.newFixedThreadPool(10),
			ParallelUtils.addProgress(chunks, progressWriter));
		return result;
	}
}
