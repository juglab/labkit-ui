/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

import bdv.export.ProgressWriterConsole;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.segmentation.SegmentationUtils;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;
import sc.fiji.labkit.ui.utils.ParallelUtils;

import java.io.File;

/**
 * @author Robert Haase
 * @author Matthias Arzt
 */
@Plugin(type = Command.class,
	menuPath = "Plugins > Labkit > Macro Recordable > Calculate Probability Map With Labkit")
public class CalculateProbabilityMapWithLabkitPlugin implements Command, Cancelable {

	@Parameter
	private Context context;

	@Parameter
	private DatasetService datasetService;

	@Parameter
	private Dataset input;

	@Parameter
	private File segmenter_file;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(required = false)
	private Boolean use_gpu = false;

	@Override
	public void run() {
		TrainableSegmentationSegmenter segmenter = new TrainableSegmentationSegmenter(context);
		segmenter.setUseGpu(use_gpu);
		segmenter.openModel(segmenter_file.getAbsolutePath());
		ImgPlus<?> imgPlus = new DatasetInputImage(input).imageForSegmentation();
		Img<FloatType> outputImg = useCache(imgPlus, segmenter) ? calculateOnCachedImg(segmenter,
			imgPlus)
			: calculateOnArrayImg(segmenter, imgPlus);
		output = datasetService.create(outputImg);
	}

	private boolean useCache(ImgPlus<?> imgPlus, Segmenter segmenter) {
		int numberOfChannels = segmenter.classNames().size();
		return Intervals.numElements(SegmentationUtils.intervalNoChannels(imgPlus)) *
			numberOfChannels > 100_000_000;
	}

	private Img<FloatType> calculateOnCachedImg(TrainableSegmentationSegmenter segmenter,
		ImgPlus<?> imgPlus)
	{
		Img<FloatType> outputImg = SegmentationUtils.createCachedProbabilityMap(segmenter, imgPlus,
			null);
		ParallelUtils.populateCachedImg(outputImg, new ProgressWriterConsole());
		return outputImg;
	}

	private Img<FloatType> calculateOnArrayImg(TrainableSegmentationSegmenter segmenter,
		ImgPlus<?> imgPlus)
	{
		int numberOfChannels = segmenter.classNames().size();
		long[] imageSize = RevampUtils.extend(Intervals.dimensionsAsLongArray(SegmentationUtils
			.intervalNoChannels(imgPlus)), numberOfChannels);
		int[] cellSize = RevampUtils.extend(segmenter.suggestCellSize(imgPlus), numberOfChannels);
		Img<FloatType> outputImg = ArrayImgs.floats(imageSize);
		ParallelUtils.applyOperationOnCells(outputImg, cellSize,
			outputCell -> segmenter.predict(imgPlus, outputCell), new ProgressWriterConsole());
		return outputImg;
	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void cancel(String reason) {

	}

	@Override
	public String getCancelReason() {
		return null;
	}
}
