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

package sc.fiji.labkit.ui.inputimage;

import mpicbg.spim.data.SpimDataException;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Cast;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SpimDataToImgPlusTest {

	@Test
	public void testOpen() {
		String filename = SpimDataToImgPlusTest.class.getResource("/export.xml").getPath();
		ImgPlus<UnsignedShortType> result = Cast.unchecked(SpimDataToImgPlus.open(filename, 0));
		assertEquals(Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME), ImgPlusViewsOld
			.getAxes(result));
		Img<IntType> expected = ArrayImgs.ints(IntStream.range(1, 33).toArray(), 2, 2, 2, 2, 2);
		ImgLib2Assert.assertImageEqualsRealType(expected, result, 0.0);
	}

	@Test(expected = SpimDataInputException.class)
	public void testExceptionForMultipleAngles() {
		String filename = SpimDataToImgPlusTest.class.getResource("/multi-angle-dataset.xml").getPath();
		SpimDataToImgPlus.open(filename, 0);
	}

	@Test(expected = SpimDataInputException.class)
	public void testExceptionForSizeMismatch() {
		String filename = SpimDataToImgPlusTest.class.getResource("/size-mismatch-dataset.xml")
			.getPath();
		SpimDataToImgPlus.open(filename, 0);
	}

	@Test
	public void testTransformationEquals() {
		AffineTransform3D a = new AffineTransform3D();
		a.set(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2);
		AffineTransform3D b = new AffineTransform3D();
		a.set(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7001, 0.8, 0.9, 1.0, 1.1, 1.2);
		assertTrue(SpimDataToImgPlus.transformationEquals(a, a));
		assertFalse(SpimDataToImgPlus.transformationEquals(a, b));
	}

}
