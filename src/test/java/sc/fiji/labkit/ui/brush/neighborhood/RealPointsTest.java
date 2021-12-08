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
	public void testDotProduct() {
		double result = RealPoints.dotProduct(a, b);
		assertEquals(13.0, result, 0.0);
	}

	@Test
	public void test() {
		RealPoint result = RealPoints.projectVectorOnto(new RealPoint(1.0, 3.0),
			new RealPoint(1.0, 1.0));
		RealPoints.assertEquals(new RealPoint(2.0, 2.0), result);
	}
}
