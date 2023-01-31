/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import sc.fiji.labkit.ui.segmentation.Segmenter;
import net.imglib2.type.NativeType;

import java.util.Arrays;
import java.util.function.Consumer;

public class DefaultCachedImageFactory implements CachedImageFactory {

	public static CachedImageFactory getInstance() {
		return new DefaultCachedImageFactory();
	}

	private DefaultCachedImageFactory() {

	}

	@Override
	public <T extends NativeType<T>> Img<T> setupCachedImage(Segmenter segmenter,
		Consumer<RandomAccessibleInterval<T>> loader, CellGrid grid, T type)
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
		return factory.create(imgDimensions, loader::accept,
			DiskCachedCellImgOptions.options().initializeCellsAsDirty(true));
	}

	private static int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}

}
