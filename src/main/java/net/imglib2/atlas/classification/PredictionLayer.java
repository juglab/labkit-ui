package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessible;
import net.imglib2.atlas.FeatureStack;
import net.imglib2.atlas.MainFrame;
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
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

public class PredictionLayer implements Classifier.Listener
{

	private final MainFrame.Extensible extensible;

	private final ClassifyingCellLoader< ? > loader;

	private final IntegerColorProvider colorProvider;

	private final RandomAccessibleContainer< VolatileARGBType > predictionContainer;

	private final FeatureStack featureStack;

	public PredictionLayer(MainFrame.Extensible extensible, ColorMapColorProvider colorProvider, Classifier classifier, FeatureStack featureStack) {
		super();
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), featureStack.grid().numDimensions());
		this.extensible = extensible;
		this.featureStack = featureStack;
		this.loader = new ClassifyingCellLoader<>(featureStack.block(), classifier);
		this.colorProvider = colorProvider;
		this.predictionContainer = new RandomAccessibleContainer<>( emptyPrediction );
		extensible.addLayer(Views.interval(predictionContainer, featureStack.interval()), "prediction");
		classifier.listeners().add( this );
	}

	public RandomAccessible< VolatileARGBType > prediction() {
		return predictionContainer;
	}

	@Override
	public void notify( final Classifier classifier, final boolean trainingSuccess )
	{
		if ( trainingSuccess )
			synchronized ( extensible.viewerSync() )
			{
				final int[] cellDimensions = new int[ featureStack.grid().numDimensions() ];
				featureStack.grid().cellDimensions( cellDimensions );
				final DiskCachedCellImgOptions factoryOptions = DiskCachedCellImgOptions.options()
						.cacheType( CacheType.BOUNDED )
						.maxCacheSize( 1000 )
						.cellDimensions( cellDimensions );
				final DiskCachedCellImgFactory< ShortType > factory = new DiskCachedCellImgFactory<>( factoryOptions );


				final DiskCachedCellImg< ShortType, ? > img = factory.create( featureStack.grid().getImgDimensions(), new ShortType(), loader );
//				final VolatileCachedCellImg< VolatileShortType, VolatileShortArray > vimg = new VolatileCachedCellImg<>( cacheOptions.grid, vtype, hints, volatileCache.unchecked()::get );


				final Converter< VolatileShortType, VolatileARGBType > conv = ( input, output ) -> {
					final boolean isValid = input.isValid();
					output.setValid( isValid );
					if ( isValid )
						output.set( colorProvider.getColor( input.get().get() ) );
				};

				final RandomAccessible< VolatileShortType > extended =
						Views.extendValue( extensible.wrapAsVolatile(img), new VolatileShortType( ( short ) LabelBrushController.BACKGROUND ) );
				final RandomAccessible< VolatileARGBType > converted = Converters.convert( extended, conv, new VolatileARGBType() );

				predictionContainer.setSource( converted );

				extensible.repaint();
			}

	}
}
