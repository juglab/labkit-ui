/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

package sc.fiji.labkit.ui.imglib2;

import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests imglib2 {@link AffineTransform3D}.
 */
public class AffineTransform3DTest {

	private AffineTransform3D scale;

	private AffineTransform3D move;

	private RealPoint a = point(1, 2, 3);

	@Before
	public void before() {
		scale = new AffineTransform3D();
		scale.scale(2);
		move = new AffineTransform3D();
		move.translate(0, 4, 5);
	}

	@Test
	public void testScale() {
		RealPoint actual = apply(scale, a);
		assertEquals(point(2, 4, 6), actual);
	}

	@Test
	public void testMove() {
		RealPoint actual = apply(move, a);
		assertEquals(point(1, 6, 8), actual);
	}

	@Test
	public void testConcatenate() {
		AffineTransform3D m = new AffineTransform3D();
		m.set(scale);
		m.concatenate(move);
		RealPoint actual = apply(m, a);
		RealPoint expected = apply(scale, apply(move, a));
		assertEquals(expected, actual);
	}

	@Test
	public void testPreConcatenate() {
		AffineTransform3D m = new AffineTransform3D();
		m.set(scale);
		m.preConcatenate(move);
		RealPoint expected = apply(move, apply(scale, a));
		RealPoint actual = apply(m, a);
		assertEquals(expected, actual);
	}

	/** @return A {@link RealPoint} initialized with the given coordinates. */
	public RealPoint point(double... position) {
		return new RealPoint(position);
	}

	/**
	 * @return The point that results from applying the {@link AffineTransform3D} to
	 *         the given point.
	 */
	private RealPoint apply(AffineTransform3D scale, RealPoint p) {
		RealPoint r = new RealPoint(3);
		scale.apply(p, r);
		return r;
	}
}
