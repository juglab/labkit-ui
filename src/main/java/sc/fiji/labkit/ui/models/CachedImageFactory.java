
package sc.fiji.labkit.ui.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import net.imglib2.type.NativeType;

import java.util.function.Consumer;

/**
 * Factory for creating a cached image that stores segmentation/prediction
 * results
 */
public interface CachedImageFactory {

	<T extends NativeType<T>> Img<T> setupCachedImage(Segmenter segmenter,
		Consumer<RandomAccessibleInterval<T>> loader,
		CellGrid grid, T type);
}
