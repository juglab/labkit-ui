package net.imglib2.atlas;

import ij.ImagePlus;
import io.scif.img.ImgIOException;
import io.scif.img.ImgSaver;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.Features;
import net.imglib2.algorithm.features.classification.Classifier;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by arzt on 23.08.17.
 */
public class BatchSegmenter {
	public static void classifyLung() throws IOException, IncompatibleTypeException, ImgIOException, InterruptedException {
		final String imgPath = "/home/arzt/Documents/20170804_LungImages/2017_08_03__0006.jpg";
		final String classifierPath = "/home/arzt/Documents/20170804_LungImages/0006.classifier";
		final Img<ARGBType> rawImg = ImageJFunctions.wrap( new ImagePlus( imgPath ) );
		Classifier classifier = Classifier.load(classifierPath);
		final int[] cellDimensions = new int[] { 256, 256 };
		RandomAccessible<ARGBType> image = Views.extendBorder(rawImg);

		Consumer<RandomAccessibleInterval<UnsignedByteType>> loader = target -> Segmenter.segment(classifier, image, target);

		Img<UnsignedByteType> segmentation = ArrayImgs.unsignedBytes(Intervals.dimensionsAsLongArray(rawImg));

		List<Callable<Void>> chunks = ParallelUtils.chunkOperation(segmentation, cellDimensions, loader);

		ParallelUtils.executeInParallel(
				Executors.newFixedThreadPool(10),
				ParallelUtils.addShowProgress(chunks)
		);

		new ImgSaver().saveImg("/home/arzt/test.tif", segmentation);
		System.out.println("finish");
	}

	private static class Segmenter {
		public static void segment(Classifier classifier, RandomAccessible<ARGBType> image, RandomAccessibleInterval<? extends IntegerType<?>> result) {
			Interval interval = result;
			RandomAccessibleInterval<FloatType> featureBlock = Features.applyOnColoredImage(classifier.features(), image, interval);
			AtlasUtils.copy(classifier.segmentLazyOnComposite(Views.collapse(featureBlock)), result);
		}
	}
}
