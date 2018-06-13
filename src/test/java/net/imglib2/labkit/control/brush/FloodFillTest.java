
package net.imglib2.labkit.control.brush;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FloodFillTest {

	int[] imageData = { 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0,
		0, 1, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 };

	private RandomAccessibleInterval<? extends BooleanType<?>> image = asBits(
		imageData, 8, 5);

	private RandomAccessibleInterval<BitType> asBits(int[] imageData,
		long... dims)
	{
		RandomAccessibleInterval<IntType> imageInts = ArrayImgs.ints(imageData,
			dims);
		return Converters.convert(imageInts, (in, out) -> out.set(in.get() > 0),
			new BitType());
	}

	int[] connectedData = { 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1,
		1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0 };

	RandomAccessibleInterval<BitType> expectedComponent = asBits(connectedData, 8,
		5);

	@Test
	public void test() {
		Point seed = new Point(2, 2);
		RandomAccessibleInterval<BitType> result = copy(image);
		LabelBrushController.floodFill(result, seed, new BitType(true));
		Views.interval(Views.pair(expectedComponent, result), expectedComponent)
			.forEach(p -> assertEquals(p.getA().get(), p.getB().get()));
	}

	private RandomAccessibleInterval<BitType> copy(
		RandomAccessibleInterval<? extends BooleanType<?>> in)
	{
		RandomAccessibleInterval<BitType> result = new SparseIterableRegion(in);
		Views.interval(Views.pair(in, result), in).forEach(p -> p.getB().set(p
			.getA().get()));
		return result;
	}

	public static void main(String... args) {
		new FloodFillTest().test();
	}
}
