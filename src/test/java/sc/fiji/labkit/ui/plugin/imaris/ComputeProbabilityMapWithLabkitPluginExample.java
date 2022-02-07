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

package sc.fiji.labkit.ui.plugin.imaris;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;

public class ComputeProbabilityMapWithLabkitPluginExample
{
	public static void main( String[] args ) throws IOException, ExecutionException, InterruptedException
	{
		// setup
		ImageJ imageJ = new ImageJ(SingletonContext.getInstance());
		imageJ.ui().showUI();

		Dataset image = imageJ.scifio().datasetIO().open(fullPath("/blobs.tif"));
		String blobsModel = fullPath("/blobs.classifier");
		Dataset expectedImage = imageJ.scifio().datasetIO().open(fullPath("/blobs_segmentation.tif"));
		// process
		Dataset output = (Dataset) imageJ.command().run(ComputeProbabilityMapWithLabkitPlugin.class, true,
			"input", image,
			"segmenter_file", blobsModel,
			"use_gpu", false)
			.get().getOutput("output");
	}

	private static String fullPath(String name) {
		return ComputeProbabilityMapWithLabkitPluginExample.class.getResource(
			name).getFile();
	}
}
