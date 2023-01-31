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

package sc.fiji.labkit.ui;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.EnumeratedAxis;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.labeling.Labeling;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class InitialLabelingTest {

	@Test
	public void testDoNotCrashWhenLabelingFileIsEmpty() throws IOException {
		File empty = File.createTempFile("labkit-InitialLabelingTest-",
			".czi.labeling");
		try {
			ImgPlus<UnsignedByteType> image = ImgPlus.wrap(ArrayImgs.unsignedBytes(2, 3));
			image.setSource(empty.getAbsolutePath().replace(".labeling", ""));
			DatasetInputImage inputImage = new DatasetInputImage(image);
			List<String> defaultLabels = Collections.emptyList();
			Labeling result = InitialLabeling.initLabeling(inputImage,
				SingletonContext.getInstance(),
				defaultLabels);
			assertNotNull(result);
		}
		finally {
			empty.delete();
		}
	}

	@Test
	public void testEnumeratedAxis() {
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(2, 3);
		EnumeratedAxis xAxis = new EnumeratedAxis(Axes.X, "mm", new double[] { 0, 0.7 });
		EnumeratedAxis yAxis = new EnumeratedAxis(Axes.Y, "mm", new double[] { 0, 0.3 });
		ImgPlus<UnsignedByteType> image = new ImgPlus<>(img, "test", xAxis, yAxis);
		DatasetInputImage inputImage = new DatasetInputImage(image);
		Labeling result = InitialLabeling.initialLabeling(SingletonContext.getInstance(), inputImage);
		assertNotNull(result);
	}
}
