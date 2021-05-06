
package net.imglib2.labkit.models;

import java.util.Objects;

public class ExtensionPoints {

	/**
	 * Factory for creating the cached image for segmentation result.
	 */
	private CachedImageFactory cachedSegmentationImageFactory = new DefaultCachedImageFactory();

	/**
	 * Factory for creating the cached image for prediction result.
	 */
	private CachedImageFactory cachedPredictionImageFactory = new DefaultCachedImageFactory();

	public CachedImageFactory getCachedSegmentationImageFactory() {
		return cachedSegmentationImageFactory;
	}

	public void setCachedSegmentationImageFactory(
		final CachedImageFactory factory)
	{
		Objects.requireNonNull(factory);
		cachedSegmentationImageFactory = factory;
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
}
