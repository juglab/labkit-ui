
package sc.fiji.labkit.ui.brush.neighborhood;

import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import static net.imglib2.test.ImgLib2Assert.assertImageEqualsRealType;
import static org.junit.Assert.assertEquals;

public class EllipsoidTest {

	@Test
	public void testBoundingBox() {
		Ellipsoid ellipsoid = new Ellipsoid(new double[] { 1, 1 }, new double[] { 0.5, 0.5 });
		Interval boundingBox = ellipsoid.boundingBox();
		assertEquals(Intervals.createMinMax(0, 0, 2, 2), boundingBox);
	}

	@Test
	public void testIterableRegion1() {
		IterableRegion<BitType> iterableRegion = Ellipsoid.asIterableRegion(new double[] { 1, 1 },
			new double[] { 0.5, 0.5 });
		Img<IntType> expected = ArrayImgs.ints(new int[] { 0, 1, 0, 1, 1, 1, 0, 1, 0 }, 3, 3);
		assertImageEqualsRealType(expected, iterableRegion, 0.0);
	}

	@Test
	public void testIterableRegion2() {
		IterableRegion<BitType> iterableRegion = Ellipsoid.asIterableRegion(new double[] { 0, 3, 1 },
			new double[] { 0.2, 2.5, 0.6 });
		Img<IntType> expected = ArrayImgs.ints(new int[] {
			0, 0, 1, 1, 1, 0, 0,
			1, 1, 1, 1, 1, 1, 1,
			0, 0, 1, 1, 1, 0, 0
		}, 1, 7, 3);
		assertImageEqualsRealType(expected, iterableRegion, 0.0);
	}

	@Test
	public void testIterableRegion3() {
		IterableRegion<BitType> iterableRegion = Ellipsoid.asIterableRegion(new double[] { 0.5, 0.5 },
			new double[] { 0, 0 });
		Img<IntType> expected = ArrayImgs.ints(new int[] {
			1, 1,
			1, 1,
		}, 2, 2);
		assertImageEqualsRealType(expected, iterableRegion, 0.0);
	}

	@Test
	public void testRoundUp() {
		assertEquals(8, Ellipsoid.roundUp(7.5));
		assertEquals(7, Ellipsoid.roundUp(7.499));
		assertEquals(-7, Ellipsoid.roundUp(-7.5));
	}

	@Test
	public void testRoundDown() {
		assertEquals(8, Ellipsoid.roundDown(7.501));
		assertEquals(7, Ellipsoid.roundDown(7.5));
		assertEquals(7, Ellipsoid.roundDown(7.499));
		assertEquals(-8, Ellipsoid.roundDown(-7.5));
	}
}
