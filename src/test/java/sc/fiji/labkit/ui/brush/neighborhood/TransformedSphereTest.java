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

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransformedSphereTest {

	private final AffineTransform3D transform = transform(0.1, 3, -2, 0.6, 1, 3,
		4, 5, -2, -5, -0.6, 8);

	@Test
	public void testTransformedSphereOutside() {
		List<RealLocalizable> notContained = Arrays.asList(new RealPoint(1.0, 1.0,
			1.0), new RealPoint(0, 1.1, 0));
		TransformedSphere sphere = new TransformedSphere(transform);
		for (RealLocalizable x : RealPoints.transform(transform, notContained))
			assertFalse(sphere.contains(x));
	}

	@Test
	public void testTransformedSphereInside() {
		List<RealLocalizable> contained = Arrays.asList(new RealPoint(0, 0, 0),
			new RealPoint(0, 1, 0));
		TransformedSphere sphere = new TransformedSphere(transform);
		for (RealLocalizable x : RealPoints.transform(transform, contained))
			assertTrue(sphere.contains(x));
	}

	@Test
	public void testTransformedSphereBoundingBox() {
		TransformedSphere sphere = new TransformedSphere(transform(1.0, 0.0, 0.0,
			4.0, 0.0, -2.0, 0.0, 0.0, -1.0, 0.0, 3.0, -7.0));
		Interval result = sphere.boundingBox();
		assertTrue(Intervals.equals(Intervals.createMinMax(3, -2, -11, 5, 2, -3),
			result));
	}

	@Test
	public void testIterableRegion() {
		TransformedSphere sphere = new TransformedSphere(transform(2, 0, 0, 0, 0, 1,
			0, 0, 0, 0, 1, 0));
		RandomAccessibleInterval<BitType> bitmap = TransformedSphere.iterableRegion(
			sphere, 2);
		Img<IntType> expected = ArrayImgs.ints(new int[] { 0, 0, 1, 0, 0, 1, 1, 1,
			1, 1, 0, 0, 1, 0, 0 }, 5, 3);
		LoopBuilder.setImages(expected, bitmap).forEachPixel((e, a) -> assertEquals(
			e.getInteger(), a.getInteger()));
	}

	private static AffineTransform3D transform(double... values) {
		assert values.length == 12;
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(values);
		return transform;
	}

}
