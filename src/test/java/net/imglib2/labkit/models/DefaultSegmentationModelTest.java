
package net.imglib2.labkit.models;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.PixelClassificationPlugin;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

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
