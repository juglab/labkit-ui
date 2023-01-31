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

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.labeling.LabelingSerializer;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class SegmentRGBImageTest {

	@Test
	public void testSegmentation()
		throws IOException, ExecutionException, InterruptedException
	{
		ImageJ imageJ = new ImageJ(SingletonContext.getInstance());
		Dataset image = imageJ.scifio().datasetIO().open(fullPath("/leaf.tif"));
		String blobsModel = fullPath("/leaf.classifier");
		// process
		Dataset output = (Dataset) imageJ.command().run(CalculateProbabilityMapWithLabkitPlugin.class,
			true,
			"input", image,
			"segmenter_file", blobsModel,
			"use_gpu", false)
			.get().getOutput("output");
		// test
		Dataset expectedImage = imageJ.scifio().datasetIO().open(fullPath("/leaf_probability_map.tif"));
		ImgLib2Assert.assertImageEqualsRealType(expectedImage, output, 0.0);
	}

	@Test
	public void testTraining() throws IOException {
		ImageJ imageJ = new ImageJ(SingletonContext.getInstance());
		Dataset image = imageJ.scifio().datasetIO().open(fullPath("/leaf.tif"));
		InputImage inputImage = new DatasetInputImage(image);
		SegmentationModel segmentationModel = new DefaultSegmentationModel(imageJ.context(),
			inputImage);
		Labeling labeling = new LabelingSerializer(SingletonContext.getInstance()).open(fullPath(
			"/leaf.tif.labeling"));
		segmentationModel.imageLabelingModel().labeling().set(labeling);
		SegmentationPlugin plugin = PixelClassificationPlugin.create();
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(plugin);
		segmenter.setUseGpu(false);
		segmenter.train(Collections.singletonList(new ValuePair<>(segmentationModel.imageLabelingModel()
			.imageForSegmentation().get(),
			segmentationModel.imageLabelingModel().labeling().get())));
		RandomAccessibleInterval<FloatType> prediction = segmenter.results(segmentationModel
			.imageLabelingModel()).prediction();

		// output
		Dataset expectedImage = imageJ.scifio().datasetIO().open(fullPath("/leaf_probability_map.tif"));
		ImgLib2Assert.assertImageEqualsRealType(expectedImage, prediction, 0.0);
	}

	private String fullPath(String name) {
		return this.getClass().getResource(name).getFile();
	}
}
