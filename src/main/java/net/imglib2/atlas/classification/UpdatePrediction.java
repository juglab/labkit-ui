package net.imglib2.atlas.classification;

import java.io.IOException;

import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessible;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.color.IntegerColorProvider;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.cache.Cache;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

public class UpdatePrediction< T extends RealType< T > > implements TrainClassifier.Listener< T >
{

	public static class CacheOptions
	{
		private final String cacheTempName;

		private final CellGrid grid;

		private final SharedQueue queue;

		public CacheOptions( final String cacheTempName, final CellGrid grid, final SharedQueue queue )
		{
			super();
			this.cacheTempName = cacheTempName;
			this.grid = grid;
			this.queue = queue;
		}
	}

	private final ViewerPanel viewer;

	private final ClassifyingCellLoader< T > loader;

	private final IntegerColorProvider colorProvider;

	private final CacheOptions cacheOptions;

	private final RandomAccessibleContainer< VolatileARGBType > predictionContainer;

	public UpdatePrediction(
			final ViewerPanel viewer,
			final ClassifyingCellLoader< T > loader,
			final IntegerColorProvider colorProvider,
			final CacheOptions cacheOptions,
			final RandomAccessibleContainer< VolatileARGBType > predictionContainer )
	{
		super();
		this.viewer = viewer;
		this.loader = loader;
		this.colorProvider = colorProvider;
		this.cacheOptions = cacheOptions;
		this.predictionContainer = predictionContainer;
	}

	private boolean wasTrainedAtLeastOnce = false;

	private Cache< Long, ? > cache = null;

	private DiskCachedCellImg< ShortType, ? > img = null;

	public Img< ShortType > getLazyImg()
	{
		if ( wasTrainedAtLeastOnce )
			return img;
		else
			return null;
	}

	@Override
	public void notify( final Classifier< Composite< T >, ?, ? > classifier, final boolean trainingSuccess ) throws IOException
	{
		if ( trainingSuccess )
			synchronized ( viewer )
			{
				final int[] cellDimensions = new int[ cacheOptions.grid.numDimensions() ];
				cacheOptions.grid.cellDimensions( cellDimensions );
				final DiskCachedCellImgOptions factoryOptions = DiskCachedCellImgOptions.options()
						.cacheType( CacheType.BOUNDED )
						.maxCacheSize( 1000 )
						.cellDimensions( cellDimensions );
				final DiskCachedCellImgFactory< ShortType > factory = new DiskCachedCellImgFactory<>( factoryOptions );


				final DiskCachedCellImg< ShortType, ? > img = factory.create( cacheOptions.grid.getImgDimensions(), new ShortType(), loader );
//				final VolatileCachedCellImg< VolatileShortType, VolatileShortArray > vimg = new VolatileCachedCellImg<>( cacheOptions.grid, vtype, hints, volatileCache.unchecked()::get );


				final Converter< VolatileShortType, VolatileARGBType > conv = ( input, output ) -> {
					final boolean isValid = input.isValid();
					output.setValid( isValid );
					if ( isValid )
						output.set( colorProvider.getColor( input.get().get() ) );
				};

				final RandomAccessible< VolatileShortType > extended =
						Views.extendValue( VolatileViews.wrapAsVolatile( img, cacheOptions.queue ), new VolatileShortType( ( short ) LabelBrushController.BACKGROUND ) );
				final RandomAccessible< VolatileARGBType > converted = Converters.convert( extended, conv, new VolatileARGBType() );

				predictionContainer.setSource( converted );

				viewer.requestRepaint();
				this.cache = img.getCache();
				this.img = img;
				wasTrainedAtLeastOnce = true;
			}

	}
}
