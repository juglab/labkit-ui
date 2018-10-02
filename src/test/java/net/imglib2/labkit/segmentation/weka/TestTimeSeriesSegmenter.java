
package net.imglib2.labkit.segmentation.weka;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.GenericComposite;
import org.junit.Test;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertArrayEquals;

public class TestTimeSeriesSegmenter {

	@Test
	public void testPredict() {
		TimeSeriesSegmenter segmenter = new TimeSeriesSegmenter(
			new SimpleSegmenter());
		Img<?> source = ArrayImgs.floats(new float[] { 0.2f, 0.3f, 0.9f }, 3);
		float[] array = new float[6];
		Img<FloatType> target = ArrayImgs.floats(array, 3, 2);
		segmenter.predict(source, target);
		assertArrayEquals(new float[] { 0.2f, 0.3f, 0.9f, 0.8f, 0.7f, 0.1f }, array,
			0.001f);
	}

	private static class SimpleSegmenter implements Segmenter {

		@Override
		public void editSettings(JFrame dialogParent) {

		}

		@Override
		public void segment(RandomAccessibleInterval<?> image,
			RandomAccessibleInterval<? extends IntegerType<?>> labels)
		{

		}

		@Override
		public void predict(RandomAccessibleInterval<?> image,
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
		public void train(List<? extends RandomAccessibleInterval<?>> image,
			List<? extends Labeling> groundTruth)
		{

		}

		@Override
		public boolean isTrained() {
			return false;
		}

		@Override
		public void saveModel(String path, boolean overwrite) throws Exception {

		}

		@Override
		public void openModel(String path) throws Exception {

		}

		@Override
		public Notifier<Consumer<Segmenter>> listeners() {
			return new Notifier<>();
		}

		@Override
		public List<String> classNames() {
			return null;
		}
	}

}
