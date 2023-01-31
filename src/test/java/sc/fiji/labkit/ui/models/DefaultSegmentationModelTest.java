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

package sc.fiji.labkit.ui.models;

import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DefaultSegmentationModelTest {

	@Test
	public void testListener() {
		BitType flag = new BitType(false);
		SegmenterListModel model = new DefaultSegmentationModel(
			new Context(), new DatasetInputImage(ArrayImgs.unsignedBytes(100, 100))).segmenterList();
		model.segmenters().notifier().addListener(flag::setOne);
		assertFalse(flag.get());
		assertEquals(0, model.segmenters().get().size());
		model.addSegmenter(PixelClassificationPlugin.create());
		assertTrue(flag.get());
		assertEquals(1, model.segmenters().get().size());
		flag.set(false);
		SegmentationItem item = model.segmenters().get().get(0);
		model.remove(item);
		assertTrue(flag.get());
		assertEquals(0, model.segmenters().get().size());
	}

	@Test
	public void testLoadClassifierWithDifferentLabels() throws IOException {
		// This test reproduced a NoSuchElementException, that appeared when
		// a classifier is loaded, that was trained on a label, whose name
		// doesn't appear in the current labeling.

		// create model
		DatasetInputImage image = new DatasetInputImage(ArrayImgs.unsignedBytes(1, 1));
		SegmentationModel model = new DefaultSegmentationModel(new Context(), image);
		// train classifier
		Labeling labeling = model.imageLabelingModel().labeling().get();
		List<Label> labels = labeling.getLabels();
		Views.iterable(labeling.getRegion(labels.get(0))).forEach(BitType::setOne);
		TrainableSegmentationSegmenter.setUseGpuPreference(model.context(), false);
		SegmentationItem item = model.segmenterList().addSegmenter(PixelClassificationPlugin.create());
		item.train(Collections.singletonList(new ValuePair<>(image
			.imageForSegmentation(), labeling)));
		// save classifier
		File tmpFile = File.createTempFile("model", ".classifier");
		tmpFile.deleteOnExit();
		item.saveModel(tmpFile.getAbsolutePath());
		// remove labels
		for (Label label : labels)
			labeling.removeLabel(label);
		// open classifier
		item.openModel(tmpFile.getAbsolutePath());
	}
}
