
package net.imglib2.labkit.models;

import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.type.NativeType;

import java.util.Arrays;

public class DefaultCachedImageFactory implements CachedImageFactory {

	public <T extends NativeType<T>> Img<T> setupCachedImage(
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
