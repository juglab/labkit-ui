
package net.imglib2.labkit.segmentation.weka;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;
import org.junit.Test;

import javax.swing.*;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class TestTimeSeriesSegmenter {

	@Test
	public void testPredict() {
		TimeSeriesSegmenter segmenter = new TimeSeriesSegmenter(
			new SimpleSegmenter());
		ImgPlus<?> source = new ImgPlus<>(ArrayImgs.floats(new float[] { 0.2f, 0.3f, 0.9f }, 1, 1, 3),
			"input", new AxisType[] { Axes.X, Axes.Y, Axes.TIME });
		float[] array = new float[6];
		Img<FloatType> target = ArrayImgs.floats(array, 1, 1, 3, 2);
		segmenter.predict(source, target);
		assertArrayEquals(new float[] { 0.2f, 0.3f, 0.9f, 0.8f, 0.7f, 0.1f }, array,
			0.001f);
	}

	private static class SimpleSegmenter implements Segmenter {

		@Override
		public void editSettings(JFrame dialogParent) {

		}

		@Override
		public void train(List<Pair<ImgPlus<?>, Labeling>> trainingData) {

		}

		@Override
		public void segment(ImgPlus<?> image,
			RandomAccessibleInterval<? extends IntegerType<?>> output)
		{

		}

		@Override
		public void predict(ImgPlus<?> image,
			RandomAccessibleInterval<? extends RealType<?>> prediction)
		{
			RandomAccessibleInterval<? extends GenericComposite<? extends RealType<?>>> output =
				Views.collapse(prediction);
			RandomAccessibleInterval<RealType<?>> input =
				(RandomAccessibleInterval<RealType<?>>) Views.interval(image, output);
			LoopBuilder.setImages(input, output).forEachPixel((i, o) -> {
				o.get(0).setReal(i.getRealDouble());
				o.get(1).setReal(1 - i.getRealDouble());
			});
		}

		@Override
		public boolean isTrained() {
			return false;
		}

		@Override
		public void saveModel(String path) {

		}

		@Override
		public void openModel(String path) {

		}

		@Override
		public List<String> classNames() {
			return null;
		}
	}

}
