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

package sc.fiji.labkit.ui.segmentation;

import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.IdentityAxis;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import net.imglib2.util.Intervals;
import org.apache.commons.lang3.ArrayUtils;
import org.scijava.Context;
import sc.fiji.labkit.pixel_classification.utils.SingletonContext;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.models.CachedImageFactory;
import sc.fiji.labkit.ui.models.DefaultCachedImageFactory;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;
import sc.fiji.labkit.ui.utils.ParallelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SegmentationTool {

	private Segmenter segmenter = null;

	private Context context = null;

	private ProgressWriter progressWriter = new ProgressWriterConsole();

	private Boolean useGpu = null;

	private final CachedImageFactory cachedImageFactory = DefaultCachedImageFactory.getInstance();

	public SegmentationTool() {

	}

	public SegmentationTool(Segmenter segmenter) {
		this.segmenter = segmenter;
	}

	public void openModel(String classifierFile) {
		Context context = this.context != null ? this.context : SingletonContext.getInstance();
		Segmenter segmenter = new TrainableSegmentationSegmenter(context);
		segmenter.openModel(classifierFile);
		setSegmenter(segmenter);
	}

	// setters

	public void setSegmenter(Segmenter segmenter) {
		this.segmenter = segmenter;
		if (useGpu != null)
			this.segmenter.setUseGpu(useGpu);
	}

	public void setContext(Context context) {
		this.context = Objects.requireNonNull(context);
	}

	public void setProgressWriter(ProgressWriter progressWriter) {
		this.progressWriter = Objects.requireNonNull(progressWriter);
	}

	public void setUseGpu(boolean useGpu) {
		this.useGpu = useGpu;
		if (this.segmenter != null)
			this.segmenter.setUseGpu(useGpu);
	}

	public ImgPlus<UnsignedByteType> segment(ImgPlus<?> image) {
		return segment(image, new UnsignedByteType());
	}

	public <T extends IntegerType<?>> ImgPlus<T> segment(ImgPlus<?> image, T type) {
		ImgPlus<?> imgPlus = new DatasetInputImage(image).imageForSegmentation();
		Img<T> outputImg = useCacheForSegmentation(imgPlus)
			? calculateSegmentationOnCachedImg(imgPlus, type)
			: calculateSegmentation(imgPlus, type);
		List<CalibratedAxis> axes = new ArrayList<>(ImgPlusViewsOld.getCalibratedAxes(imgPlus));
		axes.removeIf(axis -> axis.type() == Axes.CHANNEL);
		return new ImgPlus<>(outputImg,
			"segmentation of " + image.getName(),
			axes.toArray(new CalibratedAxis[0]));
	}

	private boolean useCacheForSegmentation(ImgPlus<?> imgPlus) {
		return Intervals.numElements(imgPlus) > 100_000_000;
	}

	private <T extends IntegerType<?>> Img<T> calculateSegmentation(
		ImgPlus<?> imgPlus, T type)
	{
		Interval outputInterval = SegmentationUtils.intervalNoChannels(imgPlus);
		int[] cellSize = segmenter.suggestCellSize(imgPlus);
		@SuppressWarnings({ "unchecked", "raw" })
		Img<T> outputImg = new ArrayImgFactory((NativeType) type).create(Intervals
			.dimensionsAsLongArray(outputInterval));
		ParallelUtils.applyOperationOnCells(outputImg, cellSize,
			outputCell -> segmenter.segment(imgPlus, outputCell), progressWriter);
		return outputImg;
	}

	private <T extends IntegerType<?>> Img<T> calculateSegmentationOnCachedImg(
		ImgPlus<?> imgPlus, T type)
	{
		Img<T> outputImg = Cast.unchecked(SegmentationUtils.createCachedSegmentation(
			segmenter, imgPlus, cachedImageFactory, Cast.unchecked(type)));
		ParallelUtils.populateCachedImg(outputImg, progressWriter);
		return outputImg;
	}

	public ImgPlus<FloatType> probabilityMap(ImgPlus<?> image) {
		ImgPlus<?> imgPlus = new DatasetInputImage(image).imageForSegmentation();
		Img<FloatType> outputImg = useCacheForProbabilityMap(imgPlus)
			? calculateOnCachedImg(imgPlus)
			: calculateProbabilityMap(imgPlus);
		List<CalibratedAxis> axes = new ArrayList<>(ImgPlusViewsOld.getCalibratedAxes(imgPlus));
		axes.removeIf(axis -> axis.type() == Axes.CHANNEL);
		axes.add(new IdentityAxis(Axes.CHANNEL));
		return new ImgPlus<>(outputImg, "probability map for " + image.getName(), axes.toArray(
			new CalibratedAxis[0]));
	}

	private boolean useCacheForProbabilityMap(ImgPlus<?> image) {
		int numberOfChannels = segmenter.classNames().size();
		return Intervals.numElements(SegmentationUtils.intervalNoChannels(image)) *
			numberOfChannels > 100_000_000;
	}

	private Img<FloatType> calculateOnCachedImg(ImgPlus<?> image) {
		Img<FloatType> outputImg = SegmentationUtils.createCachedProbabilityMap(segmenter, image,
			cachedImageFactory);
		ParallelUtils.populateCachedImg(outputImg, progressWriter);
		return outputImg;
	}

	private Img<FloatType> calculateProbabilityMap(ImgPlus<?> imgPlus) {
		int numberOfChannels = segmenter.classNames().size();
		long[] imageSize = ArrayUtils.add(Intervals.dimensionsAsLongArray(SegmentationUtils
			.intervalNoChannels(imgPlus)),
			numberOfChannels);
		int[] cellSize = ArrayUtils.add(segmenter.suggestCellSize(imgPlus), numberOfChannels);
		Img<FloatType> outputImg = ArrayImgs.floats(imageSize);
		ParallelUtils.applyOperationOnCells(outputImg, cellSize,
			outputCell -> segmenter.predict(imgPlus, outputCell), progressWriter);
		return outputImg;
	}
}
