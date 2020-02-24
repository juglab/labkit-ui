
package net.imglib2.labkit;

import bdv.export.ProgressWriterConsole;
import ij.ImagePlus;
import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imagej.ImgPlus;
import net.imagej.ops.OpEnvironment;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.weka.TrainableSegmentationSegmenter;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.utils.ParallelUtils;
import bdv.export.ProgressWriter;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Matthias Arzt
 */
public class BatchSegmenter {

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
		new ImgSaver().saveImg(outputFile.getAbsolutePath(), segmentation);
	}

	public static void classifyLung() throws IOException,
		IncompatibleTypeException, ImgIOException, InterruptedException
	{
		final Context context = new Context(OpService.class);
		final ImgPlus<?> rawImg = openImage();
		Segmenter segmenter = openClassifier(context);
		final int[] cellDimensions = new int[] { 256, 256 };
		Img<UnsignedByteType> segmentation = segment(rawImg, segmenter,
			cellDimensions, new ProgressWriterConsole());
		new ImgSaver().saveImg("/home/arzt/test.tif", segmentation);
	}

	private static ImgPlus<?> openImage() {
		final String imgPath =
			"/home/arzt/Documents/20170804_LungImages/2017_08_03__0006.jpg";
		return VirtualStackAdapter.wrap(new ImagePlus(imgPath));
	}

	private static Segmenter openClassifier(Context context) throws IOException {
		final String classifierPath =
			"/home/arzt/Documents/20170804_LungImages/0006.classifier";
		OpEnvironment ops = context.service(OpService.class);
		Segmenter segmenter = new TrainableSegmentationSegmenter(context);
		segmenter.openModel(classifierPath);
		return segmenter;
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
