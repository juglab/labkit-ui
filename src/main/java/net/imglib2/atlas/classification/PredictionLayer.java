package net.imglib2.atlas.classification;

import bdv.util.BdvStackSource;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.atlas.Extensible;
import net.imglib2.atlas.FeatureStack;
import net.imglib2.atlas.RandomAccessibleContainer;
import net.imglib2.atlas.actions.ToggleVisibility;
import net.imglib2.atlas.color.ColorMapProvider;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.awt.*;

public class PredictionLayer implements Classifier.Listener
{

	private final Extensible extensible;

	private final ColorMapProvider colorProvider;

	private final RandomAccessibleContainer< VolatileARGBType > predictionContainer;

	private final FeatureStack featureStack;
	private Img<ShortType> prediction;

	public PredictionLayer(Extensible extensible, ColorMapProvider colorProvider, Classifier classifier, FeatureStack featureStack) {
		super();
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), featureStack.grid().numDimensions());
		this.extensible = extensible;
		this.featureStack = featureStack;
		this.colorProvider = colorProvider;
		this.predictionContainer = new RandomAccessibleContainer<>( emptyPrediction );
		BdvStackSource<VolatileARGBType> source = extensible.addLayer(Views.interval(predictionContainer, featureStack.interval()), "prediction");
		extensible.addAction(new ToggleVisibility( "Segmentation", source ));
		classifier.listeners().add( this );
	}

	@Override
	public void notify(final Classifier classifier)
	{
		if ( classifier.isTrained() )
			synchronized ( extensible.viewerSync() )
			{
				updatePrediction(classifier);
				updateContainer(classifier);
				extensible.repaint();
			}
		else
			predictionContainer.setSource(ConstantUtils.constantRandomAccessible(new VolatileARGBType(Color.red.getRGB()), predictionContainer.numDimensions()));
	}

	private void updateContainer(Classifier classifier) {
		final RandomAccessibleInterval<VolatileARGBType> converted = coloredVolatileView(classifier);
		predictionContainer.setSource(Views.extendValue( converted, new VolatileARGBType(0)  ));
	}

	private RandomAccessibleInterval<VolatileARGBType> coloredVolatileView(Classifier classifier) {
		ARGBType[] colors = classifier.classNames().stream()
				.map(colorProvider.colorMap()::getColor)
				.toArray(ARGBType[]::new);

		return mapColors(colors, extensible.wrapAsVolatile(prediction));
	}

	private RandomAccessibleInterval<VolatileARGBType> mapColors(ARGBType[] colors, RandomAccessibleInterval<VolatileShortType> source) {
		final Converter< VolatileShortType, VolatileARGBType > conv = ( input, output ) -> {
			final boolean isValid = input.isValid();
			output.setValid( isValid );
			if ( isValid )
				output.set(colors[input.get().get()].get());
		};

		return Converters.convert(source, conv, new VolatileARGBType() );
	}

	public Img<ShortType> prediction() {
		return prediction;
	}

	private void updatePrediction(Classifier classifier) {
		final DiskCachedCellImgFactory< ShortType > factory = new DiskCachedCellImgFactory<>(setupDiskCachedCellImgOptions(featureStack.grid()));
		RandomAccessibleInterval<?> image = featureStack.compatibleOriginal();
		CellLoader<ShortType> loader = target -> classifier.segment(image, target);
		prediction = factory.create( featureStack.grid().getImgDimensions(), new ShortType(), loader );
	}

	private DiskCachedCellImgOptions setupDiskCachedCellImgOptions(CellGrid grid) {
		final int[] cellDimensions = new int[ grid.numDimensions() ];
		grid.cellDimensions( cellDimensions );
		return DiskCachedCellImgOptions.options()
				//.cacheType( CacheType.BOUNDED )
				//.maxCacheSize( 1000 )
				.cellDimensions( cellDimensions );
	}
}
