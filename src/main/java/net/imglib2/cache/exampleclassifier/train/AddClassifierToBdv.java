package net.imglib2.cache.exampleclassifier.train;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import bdv.img.cache.CreateInvalidVolatileCell;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.SourceAndConverter;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.Cache;
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
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.LazyCellImg;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.volatiles.VolatileUnsignedShortType;
import weka.classifiers.Classifier;

public class AddClassifierToBdv< T extends RealType< T > > implements TrainClassifier.Listener
{

	public static class CacheOptions
	{
		private final String cacheTempName;

		private final CellGrid grid;

		private final int entries;

		private final BlockingFetchQueues< Callable< ? > > queue;

		public CacheOptions( final String cacheTempName, final CellGrid grid, final int entries, final BlockingFetchQueues< Callable< ? > > queue )
		{
			super();
			this.cacheTempName = cacheTempName;
			this.grid = grid;
			this.entries = entries;
			this.queue = queue;
		}
	}

	private final BdvStackSource< ? > source;

	private final ClassifyingCellLoader< T > loader;

	private final TIntIntHashMap cmap;

	private final CacheOptions cacheOptions;

	public AddClassifierToBdv( final BdvStackSource< ? > source, final ClassifyingCellLoader< T > loader, final TIntIntHashMap cmap, final CacheOptions cacheOptions )
	{
		super();
		this.source = source;
		this.loader = loader;
		this.cmap = cmap;
		this.cacheOptions = cacheOptions;
	}

	private boolean wasTrainedAtLeastOnce = false;

	private Cache< Long, Cell< VolatileShortArray > > cache = null;

	private VolatileCache< Long, Cell< VolatileShortArray > > volatileCache = null;

	private VolatileCachedCellImg< VolatileUnsignedShortType, ? > vimg;

	public Img< UnsignedShortType > getLazyImg()
	{
		if ( wasTrainedAtLeastOnce )
			return new LazyCellImg<>( cacheOptions.grid, new UnsignedShortType(), cache.unchecked()::get );
		else
			return null;
	}

	public Img< VolatileUnsignedShortType > getVolatileImg()
	{
		return vimg;
	}


	@Override
	public void notify( final Classifier classifier, final boolean trainingSuccess ) throws IOException
	{
		if ( trainingSuccess )
		{
			loader.setClassifier( classifier );
			if ( !wasTrainedAtLeastOnce )
			{
				final UnsignedShortType type = new UnsignedShortType();
				final VolatileUnsignedShortType vtype = new VolatileUnsignedShortType();
				final Path blockcache = DiskCellCache.createTempDirectory( cacheOptions.cacheTempName, true );
				final DiskCellCache< VolatileShortArray > diskcache = new DiskCellCache<>(
						blockcache,
						cacheOptions.grid,
						loader,
						AccessIo.get( PrimitiveType.SHORT, AccessFlags.VOLATILE ),
						type.getEntitiesPerPixel()
						);
				final IoSync< Long, Cell< VolatileShortArray > > iosync = new IoSync<>( diskcache );
				cache = new GuardedStrongRefLoaderRemoverCache< Long, Cell< VolatileShortArray > >( 1000 )
						.withRemover( iosync )
						.withLoader( iosync )
						;

				final CreateInvalid< Long, Cell< VolatileShortArray > > createInvalid = CreateInvalidVolatileCell.get( cacheOptions.grid, type );
				volatileCache = new WeakRefVolatileCache<>( cache, cacheOptions.queue, createInvalid );

				final CacheHints hints = new CacheHints( LoadingStrategy.VOLATILE, 0, false );
				vimg = new VolatileCachedCellImg<>( cacheOptions.grid, vtype, hints, volatileCache.unchecked()::get );

				final Converter< VolatileUnsignedShortType, VolatileUnsignedShortType > conv1 = ( input, output ) -> {
					final boolean isValid = input.isValid();
					output.setValid( isValid );
					if ( isValid )
						output.set( input.get().get() + 1 );
				};

				final int alphaMask = UpdateColormap.alpha( 0.5f );

				final Converter< VolatileUnsignedShortType, ARGBType > conv2 = ( input, output ) -> {
					final boolean isValid = input.isValid();
					if ( isValid )
						output.set( cmap.get( input.get().get() ) );
				};

				System.out.println( "About to show img!" );

				final BdvStackSource< VolatileUnsignedShortType > stackSource = BdvFunctions.show( Converters.convert( ( RandomAccessibleInterval< VolatileUnsignedShortType > ) vimg, conv1, new VolatileUnsignedShortType() ), "prediction", BdvOptions.options().addTo( source ) );
				final List< SourceAndConverter< VolatileUnsignedShortType > > sources = stackSource.getSources();
				final int lastSourceIndex = sources.size() - 1;
				final SourceAndConverter< VolatileUnsignedShortType > lastSource = sources.remove( lastSourceIndex );
				final SourceAndConverter< VolatileUnsignedShortType > newSourceAndConverter = new SourceAndConverter<>( lastSource.getSpimSource(), conv2 );
				sources.add( newSourceAndConverter );
				stackSource.getBdvHandle().getViewerPanel().removeSource( lastSource.getSpimSource() );
				stackSource.getBdvHandle().getViewerPanel().addSource( newSourceAndConverter );
				stackSource.getBdvHandle().getViewerPanel().requestRepaint();

				System.out.println( "Added img!" );

				wasTrainedAtLeastOnce = true;
			}
			else
			{
				cache.invalidateAll();
				volatileCache.invalidateAll();
			}
		}

	}
}
