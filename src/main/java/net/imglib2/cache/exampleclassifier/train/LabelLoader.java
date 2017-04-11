package net.imglib2.cache.exampleclassifier.train;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.IoSync;
import net.imglib2.cache.UncheckedCache;
import net.imglib2.cache.img.AccessFlags;
import net.imglib2.cache.img.AccessIo;
import net.imglib2.cache.img.DirtyDiskCellCache;
import net.imglib2.cache.img.DiskCellCache;
import net.imglib2.cache.img.PrimitiveType;
import net.imglib2.cache.ref.GuardedStrongRefLoaderRemoverCache;
import net.imglib2.img.basictypeaccess.array.DirtyIntArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.LazyCellImg;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;

public class LabelLoader implements CacheLoader< Long, Cell< DirtyIntArray > >
{
	private final CellGrid grid;

	private final int background;

	public LabelLoader( final CellGrid grid, final int background )
	{
		this.grid = grid;

		this.background = background;
	}

	@Override
	public Cell< DirtyIntArray > get( final Long key ) throws Exception
	{
		final long index = key;

		final int n = grid.numDimensions();
		final long[] cellMin = new long[ n ];
		final int[] cellDims = new int[ n ];
		grid.getCellDimensions( index, cellMin, cellDims );
		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final DirtyIntArray array = new DirtyIntArray( blocksize );
		Arrays.fill( array.getCurrentStorageArray(), background );

		return new Cell<>( cellDims, cellMin, array );
	}

	public static LazyCellImg< IntType, DirtyIntArray > createImg( final LabelLoader loader, final String name, final int numCacheEntries ) throws IOException
	{
		final IntType type = new IntType();
		final Path blockcache = DiskCellCache.createTempDirectory( name, true );
		final DirtyDiskCellCache< DirtyIntArray > diskcache = new DirtyDiskCellCache<>( blockcache, loader.grid, loader, AccessIo.get( PrimitiveType.INT, AccessFlags.DIRTY ), type.getEntitiesPerPixel() );
		final IoSync< Long, Cell< DirtyIntArray > > iosync = new IoSync<>( diskcache );
		final UncheckedCache< Long, Cell< DirtyIntArray > > cache = new GuardedStrongRefLoaderRemoverCache< Long, Cell< DirtyIntArray > >( numCacheEntries ).withRemover( iosync ).withLoader( iosync ).unchecked();
		final LazyCellImg< IntType, DirtyIntArray > img = new LazyCellImg<>( loader.grid, type, cache::get );
		return img;
	}
}
