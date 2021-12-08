
package sc.fiji.labkit.ui.segmentation;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import sc.fiji.labkit.ui.inputimage.ImgPlusViewsOld;
import sc.fiji.labkit.ui.models.CachedImageFactory;
import sc.fiji.labkit.ui.models.DefaultCachedImageFactory;
import sc.fiji.labkit.ui.utils.DimensionUtils;
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

	public static Img<FloatType> createCachedProbabilityMap(Segmenter segmenter, ImgPlus<?> image,
		CachedImageFactory cachedImageFactory)
	{
		if (cachedImageFactory == null)
			cachedImageFactory = DefaultCachedImageFactory.getInstance();
		int[] cellSize = segmenter.suggestCellSize(image);
		Interval interval = intervalNoChannels(image);
		int count = segmenter.classNames().size();
		CellGrid gridWithoutChannels = new CellGrid(Intervals.dimensionsAsLongArray(interval),
			cellSize);
		CellGrid gridWithChannel = addDimensionToGrid(count, gridWithoutChannels);
		return cachedImageFactory.setupCachedImage(segmenter,
			target -> segmenter.predict(image, ensureCellSize(segmenter, cellSize, target)),
			gridWithChannel, new FloatType());
	}

	public static Img<ShortType> createCachedSegmentation(Segmenter segmenter, ImgPlus<?> image,
		CachedImageFactory cachedImageFactory)
	{
		if (cachedImageFactory == null)
			cachedImageFactory = DefaultCachedImageFactory.getInstance();
		int[] cellSize = segmenter.suggestCellSize(image);
		Interval interval = intervalNoChannels(image);
		CellGrid grid = new CellGrid(Intervals.dimensionsAsLongArray(interval), cellSize);
		return cachedImageFactory.setupCachedImage(segmenter,
			target -> segmenter.segment(image, ensureCellSize(segmenter, cellSize, target)),
			grid, new ShortType());
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

	private static int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}
}
