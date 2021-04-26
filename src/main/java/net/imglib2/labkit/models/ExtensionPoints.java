
package net.imglib2.labkit.models;

import java.util.Arrays;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

public class ExtensionPoints {

	/**
	 * Factory for creating a cached image that stores segmentation/prediction
	 * results
	 */
	public interface SetupCachedResultsImage {

		<T extends NativeType<T>> Img<T> setupCachedImage(Segmenter segmenter, CellLoader<T> loader,
			CellGrid grid, T type);
	}

	/**
	 * Factory for creating the cached image for segmentation result.
	 */
	private SetupCachedResultsImage segmentationStorageFactory = ExtensionPoints::setupCachedImage;

	/**
	 * Factory for creating the cached image for prediction result.
	 */
	private SetupCachedResultsImage predictionStorageFactory = ExtensionPoints::setupCachedImage;

	public interface PopulateResultsCallback {

		<T extends NumericType<T> & NativeType<T>> void run(SegmentationItem segmentationItem,
			RandomAccessibleInterval<T> resultsImage);
	}

	/**
	 * Is called after segmentation or prediction results image has been populated.
	 */
	private PopulateResultsCallback populateResultsCallback;

	public SetupCachedResultsImage getSegmentationStorageFactory() {
		return segmentationStorageFactory;
	}

	public void setSegmentationStorageFactory(
		final SetupCachedResultsImage factory)
	{
		segmentationStorageFactory = factory;
	}

	public SetupCachedResultsImage getPredictionStorageFactory() {
		return predictionStorageFactory;
	}

	public void setPredictionStorageFactory(
		final SetupCachedResultsImage factory)
	{
		predictionStorageFactory = factory;
	}

	public <T extends NumericType<T> & NativeType<T>> void afterPopulateResults(
		final SegmentationItem segmentationItem,
		final RandomAccessibleInterval<T> resultsImage)
	{
		if (populateResultsCallback != null)
			populateResultsCallback.run(segmentationItem, resultsImage);
	}

	public void setPopulateResultsCallback(
		final PopulateResultsCallback populateResultsCallback)
	{
		this.populateResultsCallback = populateResultsCallback;
	}

	private static <T extends NativeType<T>> Img<T> setupCachedImage(
		Segmenter segmenter, CellLoader<T> loader, CellGrid grid, T type)
	{
		final int[] cellDimensions = getCellDimensions(grid);
		final long[] imgDimensions = grid.getImgDimensions();
		Arrays.setAll(cellDimensions,
			i -> (int) Math.min(cellDimensions[i], imgDimensions[i]));
		DiskCachedCellImgOptions optional = DiskCachedCellImgOptions.options()
			// .cacheType( CacheType.BOUNDED )
			// .maxCacheSize( 1000 )
			.cellDimensions(cellDimensions).initializeCellsAsDirty(true);
		final DiskCachedCellImgFactory<T> factory =
			new DiskCachedCellImgFactory<>(type, optional);
		return factory.create(imgDimensions, loader,
			DiskCachedCellImgOptions.options().initializeCellsAsDirty(true));
	}

	private static int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}
}
