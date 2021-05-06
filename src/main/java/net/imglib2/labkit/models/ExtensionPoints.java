
package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;

import java.util.Objects;

public class ExtensionPoints {

	/**
	 * Factory for creating the cached image for segmentation result.
	 */
	private CachedImageFactory cachedSegmentationImageCallback = new DefaultCachedImageFactory();

	/**
	 * Factory for creating the cached image for prediction result.
	 */
	private CachedImageFactory cachedPredictionImageFactory = new DefaultCachedImageFactory();

	/**
	 * Is called after segmentation or prediction results image has been populated.
	 */
	private ResultsCompletedCallback resultsCompletedCallback = null;

	public interface ResultsCompletedCallback {

		void run(SegmentationItem segmentationItem, RandomAccessibleInterval<?> resultsImage);
	}

	public CachedImageFactory getCachedSegmentationImageCallback() {
		return cachedSegmentationImageCallback;
	}

	public void setCachedSegmentationImageCallback(
		final CachedImageFactory factory)
	{
		Objects.requireNonNull(factory);
		cachedSegmentationImageCallback = factory;
	}

	public CachedImageFactory getCachedPredictionImageFactory() {
		return cachedPredictionImageFactory;
	}

	public void setCachedPredictionImageFactory(
		final CachedImageFactory factory)
	{
		Objects.requireNonNull(factory);
		cachedPredictionImageFactory = factory;
	}

	public <T extends NumericType<T> & NativeType<T>> void fireResultsCompletedCallback(
		final SegmentationItem segmentationItem,
		final RandomAccessibleInterval<T> resultsImage)
	{
		if (resultsCompletedCallback != null)
			resultsCompletedCallback.run(segmentationItem, resultsImage);
	}

	public void setResultsCompletedCallback(
		final ResultsCompletedCallback resultsCompletedCallback)
	{
		this.resultsCompletedCallback = resultsCompletedCallback;
	}

}
