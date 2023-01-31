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

package sc.fiji.labkit.ui.plugin;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Cast;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.command.CommandService;
import org.scijava.io.location.FileLocation;
import org.scijava.plugin.Parameter;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import static net.imglib2.test.ImgLib2Assert.assertImageEqualsRealType;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link LabkitProbabilityMapForImagesInDirectoryPlugin}.
 */
public class LabkitProbabilityMapForImagesInDirectoryPluginTest {

	@Parameter
	DatasetIOService io;

	@Parameter
	DatasetService ds;

	@Parameter
	CommandService cmd;

	public LabkitProbabilityMapForImagesInDirectoryPluginTest() {
		SingletonContext.getInstance().inject(this);
	}

	@Test
	public void test()
		throws IOException, ExecutionException, InterruptedException
	{
		File inputDirectory = createTestInputDirectory();
		File outputDirectory = Files.createTempDirectory("labkit-test-output").toFile();
		cmd.run(LabkitProbabilityMapForImagesInDirectoryPlugin.class, true,
			"input_directory", inputDirectory,
			"file_filter", "*.tif",
			"output_directory", outputDirectory,
			"output_file_suffix", "_probability_map.tif",
			"segmenter_file", fullPath("/leaf.classifier"),
			"use_gpu", false).get();
		testOutputDirectory(outputDirectory);
	}

	private File createTestInputDirectory() throws IOException {
		File folder = Files.createTempDirectory("labkit-test-input").toFile();
		RandomAccessibleInterval<UnsignedByteType> image =
			Cast.unchecked(io.open(fullPath("/leaf.tif")));
		for (int i = 0; i < 4; i++) {
			File file = new File(folder, "image" + i + ".tif");
			saveImage(image, file);
			image = rotate(image);
		}
		return folder;
	}

	private void testOutputDirectory(File folder) throws IOException {
		RandomAccessibleInterval<? extends RealType<?>> expected = io.open(fullPath(
			"/leaf_probability_map.tif"));
		for (int i = 0; i < 4; i++) {
			File file = new File(folder, "image" + i + "_probability_map.tif");
			assertTrue("Expected output file is missing: " + file, file.exists());
			RandomAccessibleInterval<? extends RealType<?>> actual = io.open(new FileLocation(file));
			assertImageEqualsRealType(expected, actual, 0.0);
			expected = Views.zeroMin(Views.rotate(expected, 0, 1));
		}
	}

	private <T> RandomAccessibleInterval<T> rotate(RandomAccessibleInterval<T> image) {
		return Views.zeroMin(Views.rotate(image, 0, 1));
	}

	private void saveImage(RandomAccessibleInterval<UnsignedByteType> image, File file)
		throws IOException
	{
		Dataset dataset = ds.create(Views.zeroMin(image));
		dataset.axis(2).setType(Axes.CHANNEL);
		io.save(dataset, new FileLocation(file));
	}

	private String fullPath(String name) {
		return this.getClass().getResource(name).getFile();
	}

}
