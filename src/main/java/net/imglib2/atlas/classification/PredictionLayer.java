package net.imglib2.atlas.classification;

import net.imglib2.RandomAccessible;
import net.imglib2.atlas.FeatureStack;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.color.ColorMapProvider;
import net.imglib2.atlas.control.brush.LabelBrushController;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

public class PredictionLayer implements Classifier.Listener
{

	private final MainFrame.Extensible extensible;

	private final ClassifyingCellLoader loader;

	private final ColorMapProvider colorProvider;

	private final RandomAccessibleContainer< VolatileARGBType > predictionContainer;

	private final FeatureStack featureStack;

	public PredictionLayer(MainFrame.Extensible extensible, ColorMapProvider colorProvider, Classifier classifier, FeatureStack featureStack) {
		super();
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), featureStack.grid().numDimensions());
		this.extensible = extensible;
		this.featureStack = featureStack;
		this.loader = new ClassifyingCellLoader(featureStack.compatibleOriginal(), classifier);
		this.colorProvider = colorProvider;
		this.predictionContainer = new RandomAccessibleContainer<>( emptyPrediction );
		extensible.addLayer(Views.interval(predictionContainer, featureStack.interval()), "prediction");
		classifier.listeners().add( this );
	}

	public RandomAccessible< VolatileARGBType > prediction() {
		return predictionContainer;
	}

	@Override
	public void notify(final Classifier classifier)
	{
		if ( classifier.isTrained() )
			synchronized ( extensible.viewerSync() )
			{
				final Img<ShortType> img = getPrediction();

				int[] colors = classifier.classNames().stream().map(colorProvider.colorMap()::getColor).mapToInt(ARGBType::get).toArray();

				final Converter< VolatileShortType, VolatileARGBType > conv = ( input, output ) -> {
					final boolean isValid = input.isValid();
					output.setValid( isValid );
					if ( isValid )
						output.set(colors[input.get().get()]);
				};

				final RandomAccessible< VolatileShortType > extended =
						Views.extendValue( extensible.wrapAsVolatile(img), new VolatileShortType( ( short ) LabelBrushController.BACKGROUND ) );
				final RandomAccessible< VolatileARGBType > converted = Converters.convert( extended, conv, new VolatileARGBType() );

				predictionContainer.setSource( converted );

				extensible.repaint();
			}

	}

	public Img<ShortType> getPrediction() {
		final int[] cellDimensions = new int[ featureStack.grid().numDimensions() ];
		featureStack.grid().cellDimensions( cellDimensions );
		final DiskCachedCellImgOptions factoryOptions = DiskCachedCellImgOptions.options()
				//.cacheType( CacheType.BOUNDED )
				//.maxCacheSize( 1000 )
				.cellDimensions( cellDimensions );
		final DiskCachedCellImgFactory< ShortType > factory = new DiskCachedCellImgFactory<>( factoryOptions );


		return factory.create( featureStack.grid().getImgDimensions(), new ShortType(), loader );
	}
}
