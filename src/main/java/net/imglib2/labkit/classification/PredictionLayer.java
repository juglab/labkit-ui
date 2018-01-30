package net.imglib2.labkit.classification;

import bdv.util.BdvStackSource;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.classification.weka.TrainableSegmentationClassifier;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.labkit.utils.RandomAccessibleContainer;
import net.imglib2.labkit.actions.ToggleVisibility;
import net.imglib2.labkit.color.ColorMapProvider;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.type.volatiles.VolatileARGBType;
import net.imglib2.type.volatiles.VolatileShortType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.List;

public class PredictionLayer implements Classifier.Listener
{

	private final Extensible extensible;

	// This information should be stored in a segmentation model
	private final ColorMapProvider colorProvider;
	private final RandomAccessibleInterval< ? > compatibleImage;
	private CellGrid grid;
	private List<String> labels;

	private final RandomAccessibleContainer< VolatileARGBType > segmentationContainer;
	private Img<ShortType> segmentation;
	private Img<FloatType> prediction;


	public PredictionLayer( Extensible extensible, ImageLabelingModel model, Classifier classifier, boolean isTimeSeries )
	{
		this.compatibleImage = TrainableSegmentationClassifier.prepareOriginal( model.image() );
		this.grid = LabkitUtils.suggestGrid( this.compatibleImage, isTimeSeries );
		final RandomAccessible< VolatileARGBType > emptyPrediction = ConstantUtils.constantRandomAccessible( new VolatileARGBType( 0 ), compatibleImage.numDimensions() );
		this.extensible = extensible;
		this.colorProvider = model.colorMapProvider();
		this.segmentationContainer = new RandomAccessibleContainer<>( emptyPrediction );
		AffineTransform3D t = new AffineTransform3D();
		t.scale(model.scaling());
		BdvStackSource<VolatileARGBType> source = extensible.addLayer(Views.interval(segmentationContainer, compatibleImage ), "prediction", t);
		extensible.addAction(new ToggleVisibility( "Segmentation", source ));
		classifier.listeners().add( this );
	}

	@Override
	public void notify(final Classifier classifier)
	{
		if ( classifier.isTrained() )
			synchronized ( extensible.viewerSync() )
			{
				updateSegmentation(classifier);
				updateContainer(classifier);
				updatePrediction(classifier);
				updateLabels(classifier);
				extensible.repaint();
			}
		else
			segmentationContainer.setSource(ConstantUtils.constantRandomAccessible(new VolatileARGBType(Color.red.getRGB()), segmentationContainer.numDimensions()));
	}

	private void updateLabels(Classifier classifier) {
		this.labels = classifier.classNames();
	}

	private void updateContainer(Classifier classifier) {
		final RandomAccessibleInterval<VolatileARGBType> converted = coloredVolatileView(classifier);
		segmentationContainer.setSource(Views.extendValue( converted, new VolatileARGBType(0)  ));
	}

	private RandomAccessibleInterval<VolatileARGBType> coloredVolatileView(Classifier classifier) {
		ARGBType[] colors = classifier.classNames().stream()
				.map(colorProvider.colorMap()::getColor)
				.toArray(ARGBType[]::new);

		return mapColors(colors, extensible.wrapAsVolatile(segmentation));
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

	public Img<ShortType> segmentation() {
		return segmentation;
	}

	public Img<FloatType> prediction() {
		if(prediction == null)
			throw new IllegalStateException("No classifier trained yet");
		return prediction;
	}

	private void updatePrediction(Classifier classifier) {
		int count = classifier.classNames().size();
		CellGrid extended = new CellGrid(RevampUtils.extend(grid.getImgDimensions(), count), RevampUtils.extend(getCellDimensions(grid), count));
		prediction = setupCachedImage(target -> classifier.predict( compatibleImage, target), extended, new FloatType());
	}

	private void updateSegmentation(Classifier classifier) {
		segmentation = setupCachedImage(target -> classifier.segment( compatibleImage, target), grid, new ShortType());
	}

	private <T extends NativeType<T>> Img<T> setupCachedImage(CellLoader<T> loader, CellGrid grid, T type) {
		final int[] cellDimensions = getCellDimensions(grid);
		DiskCachedCellImgOptions optional = DiskCachedCellImgOptions.options()
				//.cacheType( CacheType.BOUNDED )
				//.maxCacheSize( 1000 )
				.cellDimensions(cellDimensions);
		final DiskCachedCellImgFactory< T > factory = new DiskCachedCellImgFactory<>(optional);
		return factory.create( grid.getImgDimensions(), type, loader );
	}

	private int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[ grid.numDimensions() ];
		grid.cellDimensions( cellDimensions );
		return cellDimensions;
	}

	public List<String> labels() {
		return labels;
	}
}
