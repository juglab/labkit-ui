
package net.imglib2.labkit.segmentation;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;

public class SegmentationUtils {

	private SegmentationUtils() {
		// prevent from instantiation
	}

	public static Img<FloatType> createCachedProbabilityMap(Segmenter segmenter, ImgPlus<?> image) {
		int[] cellSize = segmenter.suggestCellSize(image);
		CellLoader<FloatType> loader = target -> segmenter.predict(image, ensureCellSize(segmenter,
			cellSize, target));
		Interval interval = intervalNoChannels(image);
		int count = segmenter.classNames().size();
		CellGrid gridWithoutChannels = new CellGrid(Intervals.dimensionsAsLongArray(interval),
			cellSize);
		CellGrid gridWithChannel = addDimensionToGrid(count, gridWithoutChannels);
		return setupCachedImage(loader, gridWithChannel, new FloatType());
	}

	public static Img<ShortType> createCachedSegmentation(Segmenter segmenter, ImgPlus<?> image) {
		int[] cellSize = segmenter.suggestCellSize(image);
		CellLoader<ShortType> loader = target -> segmenter.segment(image, ensureCellSize(segmenter,
			cellSize, target));
		Interval interval = intervalNoChannels(image);
		CellGrid grid = new CellGrid(Intervals.dimensionsAsLongArray(interval), cellSize);
		return setupCachedImage(loader, grid, new ShortType());
	}

	private static CellGrid addDimensionToGrid(int size, CellGrid grid) {
		return new CellGrid(DimensionUtils.extend(grid
			.getImgDimensions(), size), DimensionUtils.extend(getCellDimensions(
				grid), size));
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

	private static <T extends NativeType<T>> Img<T> setupCachedImage(
		CellLoader<T> loader, CellGrid grid, T type)
	{
		final int[] cellDimensions = getCellDimensions(grid);
		final long[] imgDimensions = grid.getImgDimensions();
		Arrays.setAll(cellDimensions, i -> (int) Math.min(cellDimensions[i], imgDimensions[i]));
		DiskCachedCellImgOptions optional = DiskCachedCellImgOptions.options()
			// .cacheType( CacheType.BOUNDED )
			// .maxCacheSize( 1000 )
			.cellDimensions(cellDimensions).initializeCellsAsDirty(true);
		final DiskCachedCellImgFactory<T> factory = new DiskCachedCellImgFactory<>(
			type, optional);
		return factory.create(imgDimensions, loader,
			DiskCachedCellImgOptions.options().initializeCellsAsDirty(true));
	}

	private static int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}
}
