package net.imglib2.atlas.control.brush.neighboorhood;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.atlas.control.brush.neighborhood.HyperEllipsoidNeighborhood;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Matthias Arzt
 */
public class HyperEllipsoidNeighborhoodTest {

	public static void main(String... args) {
		new HyperEllipsoidNeighborhoodTest().test();
	}

	@Test
	public void test() {
		Img<BitType> img = ArrayImgs.bits(10, 10, 10);
		img.forEach(x -> x.set(false));
		long[] position = new long[] { 4, 5, 6 };
		double[] radius = new double[] { 3.5, 1.9, 3};
		HyperEllipsoidNeighborhood<BitType> neighborhood = new HyperEllipsoidNeighborhood<>(position, radius, img.randomAccess());
		Cursor<BitType> cursor = neighborhood.cursor();
		while (cursor.hasNext()) cursor.next().set(true);

		Img<BitType> expected = ellipsoidImg(img, position, radius);
		Views.interval(Views.pair(expected, img), expected).forEach(p ->
			assertEquals(p.getA(), p.getB()));
	}

	private Img<BitType> ellipsoidImg(Interval interval, long[] position, double[] radius) {
		Img<BitType> img = ArrayImgs.bits(Intervals.dimensionsAsLongArray(interval));
		Cursor<BitType> cursor = img.cursor();
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().set(contained(position, radius, cursor));
		}
		return img;
	}

	private boolean contained(long[] position, double[] radius, Cursor<BitType> cursor) {
		return IntStream.range(0, cursor.numDimensions())
				.mapToDouble(d -> sqr((cursor.getDoublePosition(d) - (double) position[d]) / radius[d])).sum() <= 1.0;
	}

	private double sqr(double v) {
		return v * v;
	}
}
