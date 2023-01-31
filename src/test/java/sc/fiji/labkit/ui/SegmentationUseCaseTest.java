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
import net.imagej.axis.AxisType;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.IntegerType;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.InputImage;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import sc.fiji.labkit.ui.segmentation.SegmentationPlugin;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SegmentationUseCaseTest {

	@Test
	public void test() {
		ImgPlus<UnsignedByteType> image = new ImgPlus<>(ArrayImgs.unsignedBytes(new byte[] { 1, 1, 2,
			2 }, 2, 2));
		InputImage inputImage = new DatasetInputImage(image);
		SegmentationModel segmentationModel = new DefaultSegmentationModel(
			new Context(), inputImage);
		addLabels(segmentationModel.imageLabelingModel());
		SegmentationPlugin plugin = PixelClassificationPlugin.create();
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(plugin);
		segmenter.train(Collections.singletonList(new ValuePair<>(image,
			segmentationModel.imageLabelingModel().labeling().get())));
		RandomAccessibleInterval<? extends IntegerType<?>> result =
			segmenter.results(segmentationModel.imageLabelingModel()).segmentation();
		List<Integer> list = new ArrayList<>();
		Views.iterable(result).forEach(x -> list.add(x.getInteger()));
		assertEquals(Arrays.asList(1, 1, 0, 0), list);
	}

	private void addLabels(ImageLabelingModel imageLabelingModel) {
		Labeling labeling = imageLabelingModel.labeling().get();
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 0 });
		ra.get().add(labeling.getLabel("foreground"));
		ra.setPosition(new long[] { 0, 1 });
		ra.get().add(labeling.getLabel("background"));
	}

	@Test
	public void testMultiChannel() throws InterruptedException {
		Img<UnsignedByteType> img = ArrayImgs.unsignedBytes(new byte[] { -1, 0, -1,
			0, -1, -1, 0, 0 }, 2, 2, 2);
		ImgPlus<UnsignedByteType> imgPlus = new ImgPlus<>(img, "Image",
			new AxisType[] { Axes.X, Axes.Y, Axes.CHANNEL });
		DatasetInputImage inputImage = new DatasetInputImage(imgPlus, BdvShowable
			.wrap(Views.hyperSlice(img, 2, 0)));

		Labeling labeling = getLabeling();
		SegmentationModel segmentationModel = new DefaultSegmentationModel(new Context(),
			inputImage);
		ImageLabelingModel imageLabelingModel = segmentationModel.imageLabelingModel();
		imageLabelingModel.labeling().set(labeling);
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(
			PixelClassificationPlugin.create());
		segmenter.train(Collections.singletonList(new ValuePair<>(imgPlus,
			imageLabelingModel.labeling().get())));
		RandomAccessibleInterval<? extends IntegerType<?>> result =
			segmenter.results(imageLabelingModel).segmentation();
		Iterator<? extends IntegerType<?>> it = Views.iterable(result).iterator();
		assertEquals(1, it.next().getInteger());
		assertEquals(0, it.next().getInteger());
		assertEquals(0, it.next().getInteger());
		assertEquals(0, it.next().getInteger());
		assertTrue(Intervals.equals(new FinalInterval(2, 2), result));
	}

	private Labeling getLabeling() {
		List<String> labels = Arrays.asList("b", "f");
		Interval interval = new FinalInterval(2, 2);
		Labeling labeling = Labeling.createEmpty(labels, interval);
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 0, 0 });
		ra.get().add(labeling.getLabel("f"));
		ra.setPosition(new long[] { 1, 0 });
		ra.get().add(labeling.getLabel("b"));
		ra.setPosition(new long[] { 0, 1 });
		ra.get().add(labeling.getLabel("b"));
		ra.setPosition(new long[] { 1, 1 });
		ra.get().add(labeling.getLabel("b"));
		return labeling;
	}

}
