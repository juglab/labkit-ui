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

import bdv.export.ProgressWriterConsole;
import java.io.File;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.segmentation.SegmentationUtils;
import sc.fiji.labkit.ui.segmentation.weka.TrainableSegmentationSegmenter;
import sc.fiji.labkit.ui.utils.DimensionUtils;
import sc.fiji.labkit.ui.utils.ParallelUtils;

/**
 * @author Igor Beati
 * @author Tobias Pietzsch
 */
@Plugin(type = Command.class,
	menuPath = "Plugins > Labkit > Macro Recordable > Compute Probability Map With Labkit")
public class ComputeProbabilityMapWithLabkitPlugin implements Command, Cancelable {

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
		Img< UnsignedByteType > outputImg = useCache( imgPlus )
				? calculateProbabilityMapOnCachedImg( segmenter, imgPlus )
				: calculateProbabilityMapOnArrayImg( segmenter, imgPlus );
		output = datasetService.create(outputImg);

		// copy input calibration to output
		ImgPlus< ? > image = imgPlus;
		if ( ImgPlusViewsOld.hasAxis( image, Axes.CHANNEL ) )
			image = ImgPlusViewsOld.hyperSlice( image, Axes.CHANNEL, 0 );
		final CalibratedAxis[] axes = new CalibratedAxis[image.numDimensions()];
		image.axes( axes );

		final CalibratedAxis[] axesWithChannel = new CalibratedAxis[axes.length + 1];
		System.arraycopy(axes, 0, axesWithChannel, 0, axes.length);
		axesWithChannel[axes.length] = new DefaultLinearAxis(Axes.CHANNEL);
		output.setAxes( axesWithChannel );
	}

	private static boolean useCache(ImgPlus<?> imgPlus) {
		return Intervals.numElements(imgPlus) > 100_000_000;
	}

	private Img<UnsignedByteType> calculateProbabilityMapOnCachedImg(TrainableSegmentationSegmenter segmenter,
		ImgPlus<?> imgPlus)
	{
		System.out.println( "ComputeProbabilityMapWithLabkitPlugin.calculateProbabilityMapOnCachedImg" );
		final ByteProbabilityMapFactory factory = new ByteProbabilityMapFactory();
		Img<FloatType> outputImg = SegmentationUtils.createCachedProbabilityMap(segmenter, imgPlus, factory);
		ParallelUtils.populateCachedImg(outputImg, new ProgressWriterConsole());
		factory.getImg().getCache().persistAll();
		return factory.getBackingImg();
	}

	private Img<UnsignedByteType> calculateProbabilityMapOnArrayImg(TrainableSegmentationSegmenter segmenter,
			ImgPlus<?> imgPlus)
	{
		System.out.println( "ComputeProbabilityMapWithLabkitPlugin.calculateProbabilityMapOnArrayImg" );
		Interval outputInterval = SegmentationUtils.intervalNoChannels(imgPlus);
		int count = segmenter.classNames().size();
		int[] cellSize = segmenter.suggestCellSize(imgPlus);
		int[] cellSizeWithChannel = DimensionUtils.extend(cellSize, count);
		long[] outputSize = Intervals.dimensionsAsLongArray(outputInterval);
		long[] outputSizeWithChannel = DimensionUtils.extend(outputSize, count);
		Img<FloatType> outputImg = ArrayImgs.floats(outputSizeWithChannel);
		ParallelUtils.applyOperationOnCells(outputImg, cellSizeWithChannel,
				outputCell -> segmenter.predict(imgPlus, outputCell), new ProgressWriterConsole());

		final int nDims = outputImg.numDimensions();
		long[] channelOffset = new long[ nDims ];
		channelOffset[ nDims - 1 ] = 1;
		long[] size = outputImg.dimensionsAsLongArray();
		size[ nDims - 1 ]--;
		final Img< UnsignedByteType > bytes = ArrayImgs.unsignedBytes( size );
		LoopBuilder.setImages( Views.interval( outputImg, channelOffset, outputImg.maxAsLongArray() ), bytes )
				.multiThreaded().forEachPixel( ( i, o ) -> o.setByte( ( byte ) ( i.get() * 255 ) ) );
		return bytes;
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
