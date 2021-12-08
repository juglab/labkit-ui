/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.brush.neighborhood;

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
