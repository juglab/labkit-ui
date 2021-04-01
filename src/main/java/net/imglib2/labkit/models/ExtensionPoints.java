
package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
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
	 * If non-{@code null}, overrides the standard factory for creating the cached
	 * image for segmentation result.
	 */
	private SetupCachedResultsImage segmentationStorageFactory;

	/**
	 * If non-{@code null}, overrides the standard factory for creating the cached
	 * image for prediction result.
	 */
	private SetupCachedResultsImage predictionStorageFactory;

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
}
