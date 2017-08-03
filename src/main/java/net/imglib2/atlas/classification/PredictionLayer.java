package net.imglib2.atlas.classification;

import java.io.IOException;

import bdv.util.volatiles.SharedQueue;
import bdv.util.volatiles.VolatileViews;
import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.color.ColorMapColorProvider;
import net.imglib2.atlas.color.IntegerColorProvider;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.DiskCachedCellImgOptions.CacheType;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

public class PredictionLayer< T extends RealType< T > > implements Classifier.Listener
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

	public PredictionLayer(CellGrid grid, ColorMapColorProvider colorProvider, Classifier classifier, SharedQueue queue, RandomAccessibleInterval<T> block, ViewerPanel viewerPanel) {
		super();
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), grid.numDimensions());
		this.viewer = viewerPanel;
		this.loader = new ClassifyingCellLoader<>(block, classifier);
		this.colorProvider = colorProvider;
		this.cacheOptions = new CacheOptions( "prediction", grid, queue);
		this.predictionContainer = new RandomAccessibleContainer<>( emptyPrediction );
		classifier.listeners().add( this );
	}

	public RandomAccessible< VolatileARGBType > prediction() {
		return predictionContainer;
	}

	@Override
	public void notify( final Classifier classifier, final boolean trainingSuccess )
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
			}

	}
}
