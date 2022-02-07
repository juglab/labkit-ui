package sc.fiji.labkit.ui.plugin.imaris;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.Cache;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.CacheRemover;
import net.imglib2.cache.IoSync;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.LoadedCellCacheLoader;
import net.imglib2.cache.ref.GuardedStrongRefLoaderRemoverCache;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.AccessFlags;
import net.imglib2.img.basictypeaccess.array.AbstractByteArray;
import net.imglib2.img.basictypeaccess.volatiles.array.DirtyVolatileFloatArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Fraction;
import sc.fiji.labkit.ui.models.CachedImageFactory;
import sc.fiji.labkit.ui.segmentation.Segmenter;

import static net.imglib2.img.basictypeaccess.AccessFlags.DIRTY;
import static net.imglib2.img.basictypeaccess.AccessFlags.VOLATILE;

class ByteProbabilityMapFactory implements CachedImageFactory
{
	private DiskCachedCellImg< UnsignedByteType, ? > backingImg;

	private DiskCachedCellImg< FloatType, ? > img;

	public DiskCachedCellImg< UnsignedByteType, ? > getBackingImg()
	{
		return backingImg;
	}

	public DiskCachedCellImg< FloatType, ? > getImg()
	{
		return img;
	}

	@Override
	public < T extends NativeType< T > > Img< T > setupCachedImage(
			final Segmenter segmenter,
			final Consumer< RandomAccessibleInterval< T > > loader,
			final CellGrid grid,
			final T type )
	{
		if ( type instanceof FloatType )
			return ( Img< T > ) setupCachedImage( interval -> loader.accept( ( RandomAccessibleInterval< T > ) interval ), grid );
		else
			throw new IllegalArgumentException( "expected FloatType" );
	}

	private Img< FloatType > setupCachedImage(
				final Consumer< ? super RandomAccessibleInterval< FloatType > > loader,
				final CellGrid grid )
	{
		final int n = grid.numDimensions();

		// We assume channels (labels) are the last dimension.
		final int channelDim = n - 1;

		// We require that each cell covers all channels (labels).
		if ( grid.cellDimension( channelDim ) < grid.imgDimension( channelDim ) )
			throw new IllegalArgumentException("expected each cell to cover all channels");

		final CacheLoader< Long, Cell< DirtyVolatileFloatArray > > backingLoader =
				LoadedCellCacheLoader.get( grid, loader::accept, new FloatType(), AccessFlags.setOf( VOLATILE, DIRTY ) );

		// for the backing image, remove the background channel
		final long[] imgDimensions = new long[ n ];
		grid.imgDimensions( imgDimensions );
		imgDimensions[ channelDim ] -= 1;
		final int[] cellDimensions = new int[ n ];
		grid.cellDimensions( cellDimensions );
		cellDimensions[ channelDim ] -= 1;

		final DiskCachedCellImg< UnsignedByteType, ? > backingImg = new DiskCachedCellImgFactory<>( new UnsignedByteType() ).create( imgDimensions, DiskCachedCellImgOptions.options().cellDimensions( cellDimensions ) );

		int numIoThreads = Runtime.getRuntime().availableProcessors();
		int maxIoQueueSize = numIoThreads * 2;
		final ConvertProbabilities< ? > converter = new ConvertProbabilities( backingLoader, backingImg.getCache(), grid );
		final IoSync< Long, Cell< DirtyVolatileFloatArray >, DirtyVolatileFloatArray > iosync = new IoSync<>(
				converter,
				numIoThreads,
				maxIoQueueSize );
		final Cache< Long, Cell< DirtyVolatileFloatArray > > cache =
				new GuardedStrongRefLoaderRemoverCache< Long, Cell< DirtyVolatileFloatArray >, DirtyVolatileFloatArray >( 4 )
						.withRemover( iosync )
						.withLoader( iosync );
		final DiskCachedCellImg< FloatType, DirtyVolatileFloatArray > probabilitiesImg = new DiskCachedCellImg<>(
				null,
				grid,
				new Fraction(),
				cache,
				iosync,
				new DirtyVolatileFloatArray( 0, true ) );
		probabilitiesImg.setLinkedType( new FloatType( probabilitiesImg ) );

		this.backingImg = backingImg;
		this.img = probabilitiesImg;

		return probabilitiesImg;
	}

	private static class ConvertProbabilities< A extends AbstractByteArray< A > > implements
			CacheLoader< Long, Cell< DirtyVolatileFloatArray > >,
			CacheRemover< Long, Cell< DirtyVolatileFloatArray >, DirtyVolatileFloatArray >
	{
		private final CacheLoader< Long, Cell< DirtyVolatileFloatArray > > backingLoader;

		private final Cache< Long, Cell< A > > cache;
		private final CellGrid grid;
		private final int n;

		private final Set< Long > written = ConcurrentHashMap.newKeySet();

		ConvertProbabilities(
				final CacheLoader< Long, Cell< DirtyVolatileFloatArray > > backingLoader,
				final Cache< Long, Cell< A > > byteCache,
				final CellGrid grid )
		{
			this.backingLoader = backingLoader;
			this.cache = byteCache;
			this.grid = grid;
			n = grid.numDimensions();
		}

		@Override
		public Cell< DirtyVolatileFloatArray > get( final Long key ) throws Exception
		{
			if ( written.contains( key ) )
			{
				final Cell< A > byteCell = cache.get( key );
				final byte[] bytes = byteCell.getData().getCurrentStorageArray();

				final int[] dimensions = new int[ n ];
				byteCell.dimensions( dimensions );

				final long[] min = new long[ n ];
				byteCell.min( min );

				int n1 = dimensions[ 0 ];
				for ( int d = 1; d < n - 1; ++d )
					n1 *= dimensions[ d ];
				final int numFgLabels = dimensions[ n - 1 ];
				final float[] floats = new float[ n1 * ( numFgLabels + 1 ) ];

				for ( int j = 0; j < numFgLabels; ++j)
					for ( int i = 0; i < n1; ++i )
						floats[ i + ( j + 1 ) * n1 ] = ( float ) UnsignedByteType.getUnsignedByte( bytes[ i + j * n1 ] ) / 255.0f;

				for ( int i = 0; i < n1; ++i ) {
					float bg = 1;
					for ( int j = 0; j < numFgLabels; ++j)
						bg -= floats[ i + ( j + 1 ) * n1 ];
					floats[ i ] = Math.max( 0, bg );
				}

				return new Cell<>( dimensions, min, new DirtyVolatileFloatArray( floats, true ) );
			}
			else
			{
				final Cell< DirtyVolatileFloatArray > cell = backingLoader.get( key );
				onRemoval( key, cell.getData() );
				return cell;
			}
		}

		@Override
		public void onRemoval( final Long key, final DirtyVolatileFloatArray valueData )
		{
			final float[] floats = valueData.getCurrentStorageArray();
			try
			{
				final Cell< A > byteCell = cache.get( key );
				final byte[] bytes = byteCell.getData().getCurrentStorageArray();

				final int[] dimensions = new int[ n ];
				byteCell.dimensions( dimensions );

				int n1 = dimensions[ 0 ];
				for ( int d = 1; d < n - 1; ++d )
					n1 *= dimensions[ d ];
				final int numFgLabels = dimensions[ n - 1 ];

				for ( int j = 0; j < numFgLabels; ++j )
					for ( int i = 0; i < n1; ++i )
						bytes[ i + j * n1 ] = ( byte ) ( floats[ i + ( j + 1 ) * n1 ] * 255 );

				written.add( key );
			}
			catch ( ExecutionException e )
			{
				throw new RuntimeException( e );
			}
		}

		@Override
		public CompletableFuture< Void > persist( final Long key, final DirtyVolatileFloatArray valueData )
		{
			onRemoval( key, valueData );
			return CompletableFuture.completedFuture( null );
		}

		@Override
		public DirtyVolatileFloatArray extract( final Cell< DirtyVolatileFloatArray > value )
		{
			return value.getData();
		}

		@Override
		public Cell< DirtyVolatileFloatArray > reconstruct( final Long key, final DirtyVolatileFloatArray valueData )
		{
			final long index = key;
			final long[] cellMin = new long[ n ];
			final int[] cellDims = new int[ n ];
			grid.getCellDimensions( index, cellMin, cellDims );
			return new Cell<>( cellDims, cellMin, valueData );
		}
	}
}
