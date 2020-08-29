
package net.imglib2.labkit.brush.neighborhood;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.List;
import java.util.stream.Collectors;

public class RealPoints {

	public static double squaredLength(RealLocalizable point) {
		double sumSquared = 0.0;
		for (int i = 0; i < point.numDimensions(); i++)
			sumSquared += sqr(point.getDoublePosition(i));
		return sumSquared;
	}

	public static double sqr(double x) {
		return x * x;
	}

	public static RealPoint projectVectorOnto(RealPoint vector,
		RealPoint direction)
	{
		return scale(dotProduct(vector, direction) / squaredLength(direction),
			direction);
	}

	public static RealPoint scale(double scale, RealPoint direction) {
		int n = direction.numDimensions();
		RealPoint result = new RealPoint(n);
		for (int d = 0; d < n; d++)
			result.setPosition(scale * direction.getDoublePosition(d), d);
		return result;
	}

	public static double length(RealLocalizable vector) {
		return Math.sqrt(squaredLength(vector));
	}

	public static RealPoint subtract(RealPoint a, RealPoint b) {
		assert a.numDimensions() == b.numDimensions();
		int n = a.numDimensions();
		RealPoint result = new RealPoint(n);
		for (int d = 0; d < n; d++)
			result.setPosition(a.getDoublePosition(d) - b.getDoublePosition(d), d);
		return result;
	}

	public static RealPoint add(RealPoint a, RealPoint b) {
		assert a.numDimensions() == b.numDimensions();
		int n = a.numDimensions();
		RealPoint result = new RealPoint(n);
		for (int d = 0; d < n; d++)
			result.setPosition(a.getDoublePosition(d) + b.getDoublePosition(d), d);
		return result;
	}

	public static double dotProduct(RealLocalizable a, RealLocalizable b) {
		double sumSquared = 0.0;
		for (int i = 0; i < a.numDimensions(); i++)
			sumSquared += a.getDoublePosition(i) * b.getDoublePosition(i);
		return sumSquared;
	}

	public static void assertEquals(RealLocalizable expected,
		RealLocalizable actual)
	{
		double delta = 0.0;
		assertEquals(expected, actual, delta);
	}

	public static void assertEquals(RealLocalizable expected,
		RealLocalizable actual, double delta)
	{
		if (!equals(expected, actual, delta)) throw new AssertionError("<actual>:" +
			actual + " <expected>:" + expected);
	}

	private static boolean equals(RealLocalizable expected,
		RealLocalizable actual, double delta)
	{
		if (expected.numDimensions() != actual.numDimensions()) return false;
		for (int d = 0; d < expected.numDimensions(); d++)
			if (Math.abs(expected.getDoublePosition(d) - actual.getDoublePosition(
				d)) > delta) return false;
		return true;
	}

	public static List<RealLocalizable> transform(AffineTransform3D transform,
		List<? extends RealLocalizable> point)
	{
		return point.stream().map(x -> transform(transform, x)).collect(Collectors
			.toList());
	}

	private static RealLocalizable transform(AffineTransform3D transform,
		RealLocalizable point)
	{
		RealPoint result = new RealPoint(point.numDimensions());
		transform.apply(point, result);
		return result;
	}
}
