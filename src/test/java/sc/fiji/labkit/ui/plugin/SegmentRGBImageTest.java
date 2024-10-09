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

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandService;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.labeling.LabelingSerializer;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import sc.fiji.labkit.ui.utils.TestResources;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class SegmentRGBImageTest {

	private static Context context;
	private static DatasetIOService io;
	private static CommandService cs;

	@BeforeClass
	public static void setUp() {
		context = new Context();
		io = context.service(DatasetIOService.class);
		cs = context.service(CommandService.class);
	}

	@AfterClass
	public static void tearDown() {
		context.dispose();
	}

	@Test
	public void testSegmentation()
		throws IOException, ExecutionException, InterruptedException
	{
		// setup
		Dataset image = io.open(TestResources.fullPath("/leaf.tif"));
		String blobsModel = TestResources.fullPath("/leaf.classifier");
		// process
		Dataset output = (Dataset) cs.run(CalculateProbabilityMapWithLabkitPlugin.class,
			true,
			"input", image,
			"segmenter_file", blobsModel,
			"use_gpu", false)
			.get().getOutput("output");
		// test
		Dataset expectedImage = io.open(
			TestResources.fullPath("/leaf_probability_map.tif"));
		ImgLib2Assert.assertImageEqualsRealType(expectedImage, output, 0.0);
	}

	@Test
	public void testTraining() throws IOException {
		Dataset image = io.open(TestResources.fullPath("/leaf.tif"));
		InputImage inputImage = new DatasetInputImage(image);
		SegmentationModel segmentationModel = new DefaultSegmentationModel(context,
			inputImage);
		Labeling labeling = new LabelingSerializer(context).open(
			TestResources.fullPath("/leaf.tif.labeling"));
		segmentationModel.imageLabelingModel().labeling().set(labeling);
		SegmentationPlugin plugin = PixelClassificationPlugin.create(context);
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(plugin);
		segmenter.setUseGpu(false);
		segmenter.train(Collections.singletonList(new ValuePair<>(segmentationModel.imageLabelingModel()
			.imageForSegmentation().get(),
			segmentationModel.imageLabelingModel().labeling().get())));
		RandomAccessibleInterval<FloatType> prediction = segmenter.results(segmentationModel
			.imageLabelingModel()).prediction();

		// output
		Dataset expectedImage = io.open(
			TestResources.fullPath("/leaf_probability_map.tif"));
		ImgLib2Assert.assertImageEqualsRealType(expectedImage, prediction, 0.0);
	}
}
