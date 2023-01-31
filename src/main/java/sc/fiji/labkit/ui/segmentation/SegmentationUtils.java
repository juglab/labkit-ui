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

import bdv.export.ProgressWriterConsole;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.apache.commons.lang3.ArrayUtils;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.models.CachedImageFactory;
import sc.fiji.labkit.ui.models.DefaultCachedImageFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;

public class SegmentationUtils {

	private SegmentationUtils() {
		// prevent from instantiation
	}

	public static Img<FloatType> createCachedProbabilityMap(Segmenter segmenter, ImgPlus<?> image,
		CachedImageFactory cachedImageFactory)
	{
		if (cachedImageFactory == null)
			cachedImageFactory = DefaultCachedImageFactory.getInstance();
		int[] cellSize = segmenter.suggestCellSize(image);
		Interval interval = intervalNoChannels(image);
		int count = segmenter.classNames().size();
		CellGrid grid = new CellGrid(Intervals.dimensionsAsLongArray(interval), cellSize);
		CellGrid gridWithChannel = addDimensionToGrid(count, grid);
		int[] cellSizeWithChannel = getCellDimensions(gridWithChannel);
		return cachedImageFactory.setupCachedImage(segmenter,
			target -> segmenter.predict(image, ensureCellSize(segmenter, cellSizeWithChannel, target)),
			gridWithChannel, new FloatType());
	}

	public static Img<UnsignedByteType> createCachedSegmentation(Segmenter segmenter,
		ImgPlus<?> image,
		CachedImageFactory cachedImageFactory)
	{
		return createCachedSegmentation(segmenter, image, cachedImageFactory, new UnsignedByteType());
	}

	public static <T extends IntegerType<T> & NativeType<T>> Img<T> createCachedSegmentation(
		Segmenter segmenter, ImgPlus<?> image, CachedImageFactory cachedImageFactory,
		T type)
	{
		if (cachedImageFactory == null)
			cachedImageFactory = DefaultCachedImageFactory.getInstance();
		int[] cellSize = segmenter.suggestCellSize(image);
		Interval interval = intervalNoChannels(image);
		CellGrid grid = new CellGrid(Intervals.dimensionsAsLongArray(interval), cellSize);
		return cachedImageFactory.setupCachedImage(segmenter,
			target -> segmenter.segment(image, ensureCellSize(segmenter, cellSize, target)),
			grid, type);
	}

	private static CellGrid addDimensionToGrid(int size, CellGrid grid) {
		long[] dimensions = ArrayUtils.add(grid.getImgDimensions(), size);
		int[] cellDimensions = ArrayUtils.add(getCellDimensions(grid), size);
		return new CellGrid(dimensions, cellDimensions);
	}

	/**
	 * Grows the give target to cellSize if
	 * {@link Segmenter#requiresFixedCellSize()} is true.
	 */
	private static <T extends NativeType<T> & NumericType<T>> RandomAccessibleInterval<T>
		ensureCellSize(
			Segmenter segmenter, int[] cellSize,
			RandomAccessibleInterval<T> target)
	{
		if (segmenter.requiresFixedCellSize()) {
			int[] targetSize = Intervals.dimensionsAsIntArray(target);
			if (!Arrays.equals(cellSize, targetSize)) {
				long[] min = Intervals.minAsLongArray(target);
				long[] max = new long[min.length];
				Arrays.setAll(max, d -> min[d] + cellSize[d] - 1);
				return Views.interval(Views.extendZero(target), min, max);
			}
		}
		return target;
	}

	public static Interval intervalNoChannels(ImgPlus<?> image) {
		return new FinalInterval(ImgPlusViewsOld.hasAxis(image, Axes.CHANNEL) ? ImgPlusViewsOld
			.hyperSlice(image, Axes.CHANNEL, 0) : image);
	}

	private static int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}
}
