package net.imglib2.labkit.models;

import java.util.List;
import java.util.stream.Collectors;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.classification.Classifier;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;

/**
 * SegmentationResultsModel is segmentation + probability map.
 * It wraps around a SegmentationModel and update whenever the Segmentation changes.
 * It's possible to listen to the SegmentationResultsModel.
 */

public class SegmentationResultsModel
{
	private final SegmentationModel model;
	private Img<ShortType > segmentation;
	private Img<FloatType > prediction;
	private List< String > labels;
	private List< ARGBType > colors;
	private Holder<String> selectedLabelHolder;

	private Notifier< Runnable > listeners = new Notifier<>();


	public SegmentationResultsModel( SegmentationModel model )
	{
		this.model = model;
		model.segmenter().listeners().add( this::segmenterTrained );
		this.selectedLabelHolder = new DefaultHolder<>("");
	}

	private void segmenterTrained( Classifier classifier )
	{
		if(classifier.isTrained())
		{
			updateSegmentation( classifier );
			updatePrediction( classifier );
			this.labels = classifier.classNames();
			this.colors = this.labels.stream().map(model.colorMap()::getColor).collect( Collectors.toList() );
			listeners.forEach( Runnable::run );
		}
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
		CellGrid grid = model.grid();
		CellGrid extended = new CellGrid(RevampUtils.extend(grid.getImgDimensions(), count), RevampUtils.extend(getCellDimensions(grid), count));
		prediction = setupCachedImage(target -> classifier.predict( model.image(), target), extended, new FloatType());
	}

	private void updateSegmentation(Classifier classifier) {
		segmentation = setupCachedImage(target -> classifier.segment( model.image(), target), model.grid(), new ShortType());
	}

	private <T extends NativeType<T> > Img<T> setupCachedImage(CellLoader<T> loader, CellGrid grid, T type) {
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

	public double scaling()
	{
		return model.scaling();
	}

	public Interval interval()
	{
		return new FinalInterval( model.image() );
	}

	public List< ARGBType > colors()
	{
		return colors;
	}

	public Notifier<Runnable> segmentationChangedListeners()
	{
		return listeners;
	}

	public String selected() {
		return selectedLabel().get();
	}

	public void setSelected(String value) {
		selectedLabel().set( value );
	}

	public Holder<String> selectedLabel() {
		return selectedLabelHolder;
	}

	public void setColor(String label, ARGBType newColor) {
		int index = labels().indexOf( label );
		colors().set( index, newColor );
		selectedLabel().notifier().forEach( l -> l.accept( selectedLabel().get() ) );
	}
}
