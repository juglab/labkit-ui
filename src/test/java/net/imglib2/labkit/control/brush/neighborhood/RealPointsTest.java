
package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.RealPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RealPointsTest {

	private final RealPoint a = new RealPoint(1.0, 2.0);

	private final RealPoint b = new RealPoint(3.0, 5.0);

	@Test
	public void testRealPointsAssertEquals() {
		RealPoints.assertEquals(new RealPoint(1.0), new RealPoint(1.0));
	}

	@Test(expected = AssertionError.class)
	public void testFailingRealPointsAssertEquals() {
		RealPoints.assertEquals(new RealPoint(1.0), new RealPoint(2.0));
	}

	@Test
	public void testAdd() {
		RealPoint result = RealPoints.add(a, b);
		RealPoints.assertEquals(new RealPoint(4.0, 7.0), result);
	}

	@Test
	public void testSub() {
		RealPoint result = RealPoints.subtract(a, b);
		RealPoints.assertEquals(new RealPoint(-2.0, -3.0), result);
	}

	@Test
	public void testSquaredLength() {
		double result = RealPoints.squaredLength(a);
		assertEquals(5.0, result, 0.0);
	}

	@Test
	public void testLength() {
		double result = RealPoints.length(new RealPoint(3.0, 4.0));
		assertEquals(5.0, result, 0.0);
	}

	@Test
	public void testScale() {
		RealPoint result = RealPoints.scale(3.0, a);
		RealPoints.assertEquals(new RealPoint(3.0, 6.0), result);
	}

	@Test
	public void testSkalarProdukt() {
		double result = RealPoints.skalarProdukt(a, b);
		assertEquals(13.0, result, 0.0);
	}

	@Test
	public void test() {
		RealPoint result = RealPoints.projectVectorOnto(new RealPoint(1.0, 3.0),
			new RealPoint(1.0, 1.0));
		RealPoints.assertEquals(new RealPoint(2.0, 2.0), result);
	}
}
