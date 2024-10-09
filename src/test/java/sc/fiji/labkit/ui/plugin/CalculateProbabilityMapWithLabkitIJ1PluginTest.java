/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
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
package sc.fiji.labkit.ui.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.macro.Interpreter;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.test.ImgLib2Assert;
import org.junit.Test;
import org.scijava.Context;
import sc.fiji.labkit.ui.utils.TestResources;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class CalculateProbabilityMapWithLabkitIJ1PluginTest {

	@Test
	public void test() throws IOException {
		try (Context context = new Context()) {
			String inputImage = TestResources.fullPath("/blobs.tif");
			String blobsModel = TestResources.fullPath("/blobs.classifier");
			String source = TestResources.fullPath("/blobs_probability_map.tif");
			File outputImage = File.createTempFile("labkit-segmentation-test", ".tif");
			String macroTemplate = "close('*');\n" +
				"open('INPUT_TIF');\n" +
				"run('Calculate Probability Map With Labkit (IJ1)', 'segmenter_file=SEGMENTER_FILE use_gpu=false');\n" +
				"selectImage('probability map for blobs.tif');\n" +
				"saveAs('Tiff', 'OUTPUT_TIF');\n" +
				"close('*');\n";
			String macro = macroTemplate
				.replace('\'', '"')
				.replace("INPUT_TIF", inputImage)
				.replace("SEGMENTER_FILE", blobsModel)
				.replace("OUTPUT_TIF", outputImage.getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
			new Interpreter().run(macro);
			assertTrue(outputImage.exists());
			assertTrue(outputImage.length() > 0);
			ImagePlus expected = IJ.openImage(source);
			ImagePlus result = IJ.openImage(outputImage.getAbsolutePath());
			ImgLib2Assert.assertImageEquals(VirtualStackAdapter.wrap(expected),
				VirtualStackAdapter.wrap(result), Object::equals);
		}
	}
}
