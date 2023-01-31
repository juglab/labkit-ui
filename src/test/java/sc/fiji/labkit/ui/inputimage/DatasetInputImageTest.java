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

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatasetInputImageTest {

	DatasetInputImage inputImage = inputImage5d();

	@Test
	public void testImageForSegmentation() {
		RandomAccessibleInterval<?> result = inputImage.imageForSegmentation();
		assertTrue(Util.getTypeFromInterval(result) instanceof RealType);
		assertTrue(Intervals.equals(new FinalInterval(4, 5, 2, 4, 6), result));
	}

	@Test
	public void testAxes() {
		List<CalibratedAxis> result = ImgPlusViewsOld.getCalibratedAxes(inputImage
			.imageForSegmentation());
		assertEquals(Axes.X, result.get(0).type());
		assertEquals(Axes.Y, result.get(1).type());
		assertEquals(Axes.Z, result.get(2).type());
		assertEquals(Axes.CHANNEL, result.get(3).type());
		assertEquals(Axes.TIME, result.get(4).type());
		assertEquals(5, result.size());
	}

	private static DatasetInputImage inputImage5d() {
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(4, 5, 2, 4, 6);
		ImgPlus<UnsignedByteType> imgPlus = new ImgPlus<>(image, "title",
			new AxisType[] { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME });
		return new DatasetInputImage(imgPlus, BdvShowable.wrap(Views.hyperSlice(
			image, 3, 0)));
	}

}
