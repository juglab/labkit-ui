
package demo;

import ij.ImagePlus;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabkitFrame;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;
import org.scijava.Context;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

public class CustomSegmenterDemo {

	public static void main(String... args) {
		Img<? extends NumericType<?>> image = ImageJFunctions.wrap(new ImagePlus(
			"https://imagej.nih.gov/ij/images/AuPbSn40-2.jpg"));
		Context context = new Context();
		DefaultSegmentationModel segmentationModel = new DefaultSegmentationModel(
			new DatasetInputImage(image), context, MySegmenter::new);
		LabkitFrame.show(segmentationModel, "Demonstrate other Segmenter");
	}

	private static class MySegmenter implements Segmenter {

		private MeanCalculator foreground;
		private MeanCalculator others;
		private Thresholder thresholder = null;

		public MySegmenter(final Context context, final InputImage inputImage) {

		}

		@Override
		public void editSettings(JFrame dialogParent) {

		}

		@Override
		public void train(
			List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> data)
		{
			foreground = new MeanCalculator();
			others = new MeanCalculator();
			for (Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling> imageAndLabeling : data) {
				RandomAccessibleInterval<? extends RealType<?>> image = grayScale(
					imageAndLabeling.getA());

				Labeling labeling = imageAndLabeling.getB();
				Map<Label, IterableRegion<BitType>> regions = labeling
					.iterableRegions();
				for (Label label : regions.keySet()) {
					IterableRegion<BitType> region = regions.get(label);
					MeanCalculator meanCalculator = label.name().equals("foreground")
						? foreground : others;
					meanCalculator.addSample(image, region);
				}
			}
			thresholder = new Thresholder(others.mean(), foreground.mean());
		}

		private RandomAccessibleInterval<? extends RealType<?>> grayScale(
			RandomAccessibleInterval<?> a)
		{
			Object pixel = Util.getTypeFromInterval(a);
			if (pixel instanceof RealType)
				return (RandomAccessibleInterval<? extends RealType<?>>) a;
			if (pixel instanceof ARGBType) return colorToGrayScale(
				(RandomAccessibleInterval<ARGBType>) a);
			throw new CancellationException("Pixel type must be color or grayscale");
		}

		private RandomAccessibleInterval<? extends RealType<?>> colorToGrayScale(
			RandomAccessibleInterval<ARGBType> a)
		{
			return Converters.convert(a, (i, o) -> o.setReal(i.get()),
				new DoubleType());
		}

		@Override
		public void segment(RandomAccessibleInterval<?> image,
			RandomAccessibleInterval<? extends IntegerType<?>> outputSegmentation)
		{
			RandomAccessibleInterval<? extends RealType<?>> input = Views.interval(
				grayScale(image), outputSegmentation);
			LoopBuilder.setImages(input, outputSegmentation).forEachPixel((i, o) -> o
				.setReal(thresholder.segment(i.getRealDouble())));
		}

		@Override
		public void predict(RandomAccessibleInterval<?> image,
			RandomAccessibleInterval<? extends RealType<?>> outputProbabilityMap)
		{
			RandomAccessibleInterval<? extends GenericComposite<? extends RealType<?>>> output =
				Views.collapse(outputProbabilityMap);
			RandomAccessibleInterval<? extends RealType<?>> input = Views.interval(
				grayScale(image), output);
			LoopBuilder.setImages(input, output).forEachPixel((i, o) -> {
				double p = thresholder.predict(i.getRealDouble());
				o.get(0).setReal(p);
				o.get(1).setReal(1 - p);
			});
		}

		@Override
		public boolean isTrained() {
			return thresholder != null;
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
	}

	private static class MeanCalculator {

		private double sum = 0;
		private long count = 0;

		private void addSample(RandomAccessibleInterval<?> image,
			IterableRegion<BitType> region)
		{
			Cursor<Void> cursor = region.cursor();
			RandomAccess<? extends RealType<?>> randomAccess =
				(RandomAccess<? extends RealType<?>>) image.randomAccess();
			while (cursor.hasNext()) {
				cursor.fwd();
				randomAccess.setPosition(cursor);
				sum += randomAccess.get().getRealDouble();
				count++;
			}
		}

		public double mean() {
			return sum / count;
		}
	}

	private static class Thresholder {

		private final double background;
		private final double foreground;

		public Thresholder(double background, double foreground) {
			this.background = background;
			this.foreground = foreground;
		}

		public double predict(double value) {
			return rangeZeroToOne((value - background) / (foreground - background));
		}

		public int segment(double value) {
			return predict(value) > 0.5 ? 1 : 0;
		}

		private double rangeZeroToOne(double v) {
			return Math.min(Math.max(v, 0), 1);
		}
	}
}
