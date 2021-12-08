
package sc.fiji.labkit.ui.models;

import java.util.Objects;

public class ExtensionPoints {

	/**
	 * Factory for creating the cached image for segmentation result.
	 */
	private CachedImageFactory cachedSegmentationImageFactory;

	/**
	 * Factory for creating the cached image for prediction result.
	 */
	private CachedImageFactory cachedPredictionImageFactory;

	public CachedImageFactory getCachedSegmentationImageFactory() {
		return cachedSegmentationImageFactory;
	}

	public void setCachedSegmentationImageFactory(
		final CachedImageFactory factory)
	{
		cachedSegmentationImageFactory = factory;
	}

	public CachedImageFactory getCachedPredictionImageFactory() {
		return cachedPredictionImageFactory;
	}

	public void setCachedPredictionImageFactory(
		final CachedImageFactory factory)
	{
		cachedPredictionImageFactory = factory;
	}
}
