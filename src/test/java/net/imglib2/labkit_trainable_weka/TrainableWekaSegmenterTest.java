
package net.imglib2.labkit_trainable_weka;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import ij.ImagePlus;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ValuePair;
import org.junit.Test;
import org.scijava.Context;
import trainableSegmentation.WekaSegmentation;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.DoubleStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class TrainableWekaSegmenterTest {

	public static void main(String... args) {
		ImagePlus image = new ImagePlus(
			"https://imagej.nih.gov/ij/images/AuPbSn40-2.jpg");
		ImagePlus labels = new ImagePlus("/home/arzt/AuPbSn40.jpg.labels.tif");
		WekaSegmentation wekaSegmentation = new WekaSegmentation(image);
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

	private static void subtractOne(ImagePlus labels) {
		ImagePlusAdapter.wrapFloat(labels).forEach(x -> x.setReal(x
			.getRealDouble() - 1));
	}

	@Test
	public void testSegmentation() {
		Img<UnsignedByteType> image = exampleImage();
		Segmenter segmenter = trainSegmenter(image, exampleLabeling());
		byte[] pixels = new byte[4];
		Img<UnsignedByteType> result = ArrayImgs.unsignedBytes(pixels, 2, 2);
		segmenter.segment(image, result);
		assertArrayEquals(new byte[] { 1, 0, 0, 1 }, pixels);
	}

	@Test
	public void testPrediction() {
		Img<UnsignedByteType> image = exampleImage();
		Segmenter segmenter = trainSegmenter(image, exampleLabeling());
		double[] pixels = new double[8];
		Img<DoubleType> result = ArrayImgs.doubles(pixels, 2, 2, 2);
		segmenter.predict(image, result);
		System.out.println(Arrays.toString(pixels));
		assertArrayEquals(new Boolean[] { false, true, true, false, true, false,
			false, true }, DoubleStream.of(pixels).mapToObj(x -> x > 0.5).toArray(
				Boolean[]::new));
	}

	@Test
	public void testColorSegmentation() {
		Img<ARGBType> image = exampleColorImage();
		TrainableWekaSegmenter segmenter = trainSegmenter(image, exampleLabeling());
		byte[] pixels = new byte[4];
		Img<UnsignedByteType> result = ArrayImgs.unsignedBytes(pixels, 2, 2);
		segmenter.segment(image, result);
		assertArrayEquals(new byte[] { 1, 0, 0, 1 }, pixels);
	}

	private Img<ARGBType> exampleColorImage() {
		int red = Color.red.getRGB();
		int blue = Color.blue.getRGB();
		return ArrayImgs.argbs(new int[] { red, blue, blue, red }, 2, 2);
	}

	private TrainableWekaSegmenter trainSegmenter(
		Img<? extends NumericType<?>> image, Labeling labels)
	{
		TrainableWekaSegmenter segmenter = new TrainableWekaSegmenter(new Context(),
			new DefaultInputImage(image));
		segmenter.train(Collections.singletonList(new ValuePair<>(image, labels)));
		return segmenter;
	}

	private Img<UnsignedByteType> exampleImage() {
		return ArrayImgs.unsignedBytes(new byte[] { 0, 1, 1, 0 }, 2, 2);
	}

	private Labeling exampleLabeling() {
		Labeling labels = Labeling.fromStrings(new String[] { "foreground",
			"background", null, null }, 2, 2);
		return labels;
	}

	@Test
	public void test3DSegmentation() {
		fail("TODO");
	}
}
