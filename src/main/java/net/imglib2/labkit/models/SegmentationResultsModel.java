package net.imglib2.labkit.models;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SegmentationResultsModel is segmentation + probability map.
 * It wraps around a SegmentationModel and update whenever the Segmentation changes.
 * It's possible to listen to the SegmentationResultsModel.
 */

public class SegmentationResultsModel
{
	private final SegmentationModel model;
	private boolean hasResults = false;
	private RandomAccessibleInterval<ShortType > segmentation;
	private RandomAccessibleInterval<FloatType > prediction;
	private List< String > labels = Collections.emptyList();
	private List< ARGBType > colors = Collections.emptyList();

	private Notifier< Runnable > listeners = new Notifier<>();


	public SegmentationResultsModel( SegmentationModel model, Segmenter segmenter )
	{
		this.model = model;
		segmentation = dummy( new ShortType() );
		prediction = dummy( new FloatType() );
		segmenter.listeners().add( this::segmenterTrained );
	}

	private void segmenterTrained( Segmenter segmenter )
	{
		if( segmenter.isTrained())
		{
			updateSegmentation( segmenter );
			updatePrediction( segmenter );
			this.labels = segmenter.classNames();
			this.colors = this.labels.stream().map(model.colorMap()::getColor).collect( Collectors.toList() );
			hasResults = true;
			listeners.forEach( Runnable::run );
		}
	}

	public RandomAccessibleInterval<ShortType> segmentation() {
		return segmentation;
	}

	private <T> RandomAccessibleInterval<T> dummy( T value )
	{
		FinalInterval interval = new FinalInterval( model.grid().getImgDimensions() );
		return ConstantUtils.constantRandomAccessibleInterval( value, interval.numDimensions(), interval );
	}

	public RandomAccessibleInterval<FloatType> prediction() {
		return prediction;
	}

	private void updatePrediction(Segmenter segmenter ) {
		int count = segmenter.classNames().size();
		CellGrid grid = model.grid();
		CellGrid extended = new CellGrid(RevampUtils.extend(grid.getImgDimensions(), count), RevampUtils.extend(getCellDimensions(grid), count));
		prediction = setupCachedImage(target -> segmenter.predict( model.image(), target), extended, new FloatType());
	}

	private void updateSegmentation(Segmenter segmenter ) {
		segmentation = setupCachedImage(target -> segmenter.segment( model.image(), target), model.grid(), new ShortType());
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

	public AffineTransform3D transformation()
	{
		return model.labelTransformation();
	}

	public boolean hasResults()
	{
		return hasResults;
	}
}
