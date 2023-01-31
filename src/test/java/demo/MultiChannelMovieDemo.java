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

package demo;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.FinalInterval;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import sc.fiji.labkit.ui.LabkitFrame;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.DefaultSegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.segmentation.weka.PixelClassificationPlugin;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Localizables;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import org.junit.Test;
import org.scijava.Context;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Two demos showing that the segmentation of a multi channel image with Labkit
 * works. Simple for the segmentation of a 5d image.
 */
public class MultiChannelMovieDemo {

	public static void main(String... args) {
		main2();
	}

	private static void main1() {
		byte[] pixels = { -1, -1, -1, -1, 0, 0, 0, 0, -1, 0, -1, 0, -1, 0, -1, 0 };
		ArrayImg<UnsignedByteType, ByteArray> image = ArrayImgs.unsignedBytes(
			pixels, 2, 2, 2, 2);
		assertEquals(16, pixels.length);
		AxisType[] axes = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL };
		DatasetInputImage inputImage = new DatasetInputImage(new ImgPlus<>(image,
			"", axes), BdvShowable.wrap(Views.hyperSlice(image, 3, 0)));
		LabkitFrame.showForImage(new Context(), inputImage);
	}

	private static void main2() {
		DatasetInputImage inputImage = inputImage5d();
		LabkitFrame.showForImage(new Context(), inputImage);
	}

	private static DatasetInputImage inputImage5d() {
		Img<UnsignedByteType> image = image5d();
		AxisType[] axes = { Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL, Axes.TIME };
		DatasetInputImage inputImage = new DatasetInputImage(new ImgPlus<>(image,
			"title", axes), BdvShowable.wrap(Views.hyperSlice(image, 3, 0)));
		return inputImage;
	}

	private static Img<UnsignedByteType> image5d() {
		RandomAccessibleInterval<Localizable> position = Views.interval(Localizables
			.randomAccessible(5), new FinalInterval(20, 10, 10, 2, 20));
		RandomAccessibleInterval<UnsignedByteType> rai = Converters.convert(
			position, (in, out) -> {
				long x = in.getLongPosition(0);
				long c = in.getLongPosition(3);
				long t = in.getIntPosition(4);
				out.set(x < 4 * c + t ? 255 : 0);
			}, new UnsignedByteType());
		return ImgView.wrap(rai, new ArrayImgFactory<>(new UnsignedByteType()));
	}

	@Test
	public void testInputImageImageForSegmentation() {
		DatasetInputImage inputImage = inputImage5d();
		SegmentationModel segmentationModel = new DefaultSegmentationModel(
			new Context(), inputImage);
		SegmentationItem segmenter = segmentationModel.segmenterList().addSegmenter(
			PixelClassificationPlugin.create());
		Labeling labeling1 = labeling5d();
		segmentationModel.imageLabelingModel().labeling().set(labeling1);
		segmenter.train(Collections.singletonList(new ValuePair<>(inputImage
			.imageForSegmentation(), labeling1)));
		RandomAccessibleInterval<UnsignedByteType> result =
			segmenter.results(segmentationModel.imageLabelingModel()).segmentation();
		Labeling labeling = labeling5d();
		LoopBuilder.setImages(labeling, result).forEachPixel((l, r) -> {
			if (l.contains("foreground")) assertEquals(1, r.get());
			if (l.contains("background")) assertEquals(0, r.get());
		});
	}

	private Labeling labeling5d() {
		Labeling labeling = Labeling.createEmpty(Arrays.asList("background",
			"foreground"), new FinalInterval(20, 10, 10, 20));
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		ra.setPosition(new long[] { 1, 0, 0, 1 });
		ra.get().add(labeling.getLabel("foreground"));
		ra.setPosition(new long[] { 4, 0, 0, 1 });
		ra.get().add(labeling.getLabel("foreground"));
		ra.setPosition(new long[] { 5, 0, 0, 1 });
		ra.get().add(labeling.getLabel("background"));
		ra.setPosition(new long[] { 0, 0, 0, 1 });
		ra.get().add(labeling.getLabel("background"));
		return labeling;
	}
}
