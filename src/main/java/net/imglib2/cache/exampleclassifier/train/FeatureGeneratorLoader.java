package net.imglib2.cache.exampleclassifier.train;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.IoSync;
import net.imglib2.cache.img.AccessFlags;
import net.imglib2.cache.img.AccessIo;
import net.imglib2.cache.img.DiskCellCache;
import net.imglib2.cache.img.PrimitiveType;
import net.imglib2.cache.queue.BlockingFetchQueues;
import net.imglib2.cache.ref.GuardedStrongRefLoaderRemoverCache;
import net.imglib2.cache.ref.WeakRefVolatileCache;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.CreateInvalid;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.cache.volatiles.VolatileCache;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileFloatArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.LazyCellImg;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileFloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

public class FeatureGeneratorLoader< S extends RealType< S > > implements CacheLoader< Long, Cell< VolatileFloatArray > >
{
	private final CellGrid grid;

	private final FeatureGenerator< S, FloatType > generator;

	public FeatureGeneratorLoader(
			final CellGrid grid,
			final FeatureGenerator< S, FloatType > generator )
	{
		this.grid = grid;
		this.generator = generator;
	}

	@Override
	public Cell< VolatileFloatArray > get( final Long key ) throws Exception
	{
		final long index = key;

		final int n = grid.numDimensions();
		final long[] cellMin = new long[ n ];
		final int[] cellDims = new int[ n ];
		grid.getCellDimensions( index, cellMin, cellDims );

		final FeatureGenerator< S, FloatType > generator = this.generator.copy();

		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final VolatileFloatArray array = new VolatileFloatArray( blocksize, true );

		final Img< FloatType > img = ArrayImgs.floats( array.getCurrentStorageArray(), Util.int2long( cellDims ) );
		generator.generateFeatures( Views.translate( img, cellMin ) );

		return new Cell<>( cellDims, cellMin, array );
	}

	public static < S extends RealType< S > > Pair< Img< FloatType >, Img< VolatileFloatType > > createFeatures( final FeatureGeneratorLoader< S > loader, final String name, final int numEntries, final BlockingFetchQueues< Callable< ? > > queue ) throws IOException
	{
		final FloatType type = new FloatType();
		final VolatileFloatType vtype = new VolatileFloatType();

		final Path blockcache = DiskCellCache.createTempDirectory( name, true );
		final DiskCellCache< VolatileFloatArray > diskcache = new DiskCellCache<>( blockcache, loader.grid, loader, AccessIo.get( PrimitiveType.FLOAT, AccessFlags.VOLATILE ), type.getEntitiesPerPixel() );
		final IoSync< Long, Cell< VolatileFloatArray > > iosync = new IoSync<>( diskcache );
		final Cache< Long, Cell< VolatileFloatArray > > cache = new GuardedStrongRefLoaderRemoverCache< Long, Cell< VolatileFloatArray > >( numEntries ).withRemover( iosync ).withLoader( iosync );

		final LazyCellImg< FloatType, VolatileFloatArray > img = new LazyCellImg<>( loader.grid, type, cache.unchecked()::get );

		final CreateInvalid< Long, Cell< VolatileFloatArray > > createInvalid = CreateInvalidVolatileCell.get( loader.grid, type );
		final VolatileCache< Long, Cell< VolatileFloatArray > > volatileCache = new WeakRefVolatileCache<>( cache, queue, createInvalid );

		final CacheHints hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
		final VolatileCachedCellImg< VolatileFloatType, ? > vimg = new VolatileCachedCellImg<>( loader.grid, vtype, hints, volatileCache.unchecked()::get );

		return new ValuePair<>( img, vimg );

	}
}