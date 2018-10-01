
package net.imglib2.labkit.control.brush;

import net.imglib2.Point;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.util.IterableRandomAccessibleRegion;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.type.BooleanType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class FloodFillTest {

	int[] imageDataA = { 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1,
		1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	int[] imageDataB = { 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1,
		1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	private RandomAccessibleInterval<BitType> imageA = asBits(imageDataA, 8, 5);
	private RandomAccessibleInterval<BitType> imageB = asBits(imageDataB, 8, 5);

	private RandomAccessibleInterval<BitType> asBits(int[] imageData,
		long... dims)
	{
		RandomAccessibleInterval<IntType> imageInts = ArrayImgs.ints(imageData,
			dims);
		return Converters.convert(imageInts, (in, out) -> out.set(in.get() > 0),
			new BitType());
	}

	int[] connectedData = { 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1,
		1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	RandomAccessibleInterval<BitType> expectedComponent = asBits(connectedData, 8,
		5);

	@Test
	public void test() {
		Point seed = new Point(2, 2);
		Map<String, IterableRegion<BitType>> map = new HashMap<>();
		map.put("a", IterableRandomAccessibleRegion.create(imageA));
		map.put("b", IterableRandomAccessibleRegion.create(imageB));
		map.put("c", new SparseIterableRegion(imageA));
		Labeling labeling = Labeling.fromMap(map);
		Label c = labeling.getLabel("c");
		FloodFillController.floodFillSet(labeling, seed, Collections.singleton(c));
		RandomAccessibleInterval<BitType> result = labeling.getRegion(c);
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
