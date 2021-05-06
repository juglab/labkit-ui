
package net.imglib2.labkit.models;

import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.NativeType;

/**
 * Factory for creating a cached image that stores segmentation/prediction
 * results
 */
public interface CachedImageFactory {

	// TODO: Ask tobi why this needs the segmenter.
	<T extends NativeType<T>> Img<T> setupCachedImage(Segmenter segmenter, CellLoader<T> loader,
		CellGrid grid, T type);
}
