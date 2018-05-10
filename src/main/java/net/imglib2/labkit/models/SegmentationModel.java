package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class SegmentationModel
{

	private final ImageLabelingModel imageLabelingModel;
	private final Holder< SegmentationItem > selectedSegmenter;
	private final Supplier< Segmenter > segmenterFactory;
	private List< SegmentationItem > segmenters = new ArrayList<>();
	private final RandomAccessibleInterval< ? > compatibleImage;
	private final CellGrid grid;

	public SegmentationModel( RandomAccessibleInterval< ? > compatibleImage, ImageLabelingModel imageLabelingModel, Supplier<Segmenter> segmenterFactory )
	{
		this.imageLabelingModel = imageLabelingModel;
		this.compatibleImage = compatibleImage;
		this.grid = LabkitUtils.suggestGrid( this.compatibleImage, imageLabelingModel.isTimeSeries() );
		this.segmenterFactory = segmenterFactory;
		this.selectedSegmenter = new DefaultHolder<>( addSegmenter() );
	}

	public Labeling labeling() {
		return imageLabelingModel.labeling().get();
	}

	public RandomAccessibleInterval< ? > image() {
		return compatibleImage;
	}

	public CellGrid grid() {
		return grid;
	}

	public List<SegmentationItem> segmenters() {
		return segmenters;
	}

	public Holder<SegmentationItem> selectedSegmenter() { return selectedSegmenter; }

	public ColorMap colorMap() {
		return imageLabelingModel.colorMapProvider().colorMap();
	}

	public AffineTransform3D labelTransformation()
	{
		return imageLabelingModel.labelTransformation();
	}

	public SegmentationItem addSegmenter()
	{
		SegmentationItem segmentationItem = new SegmentationItem( this, segmenterFactory.get() );
		this.segmenters.add( segmentationItem );
		return segmentationItem;
	}

	public < T extends IntegerType<T> & NativeType<T> > List<RandomAccessibleInterval<T>> getSegmentations( T type )
	{
		RandomAccessibleInterval< ? > image = image();
		Stream< Segmenter > trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(
				segmenter -> {
					RandomAccessibleInterval<T> labels = new CellImgFactory<T>().create(image, type);
					segmenter.segment( image, labels);
					return labels;
				}
		).collect( Collectors.toList());
	}

	public List< RandomAccessibleInterval< FloatType > > getPredictions()
	{
		RandomAccessibleInterval< ? > image = image();
		Stream< Segmenter > trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(
				segmenter -> {
					int numberOfClasses = segmenter.classNames().size();
					RandomAccessibleInterval<FloatType> prediction = new CellImgFactory<FloatType>().create(
							RevampUtils.appendDimensionToInterval(image, 0, numberOfClasses - 1),
							new FloatType());
					segmenter.predict(image, prediction);
					return prediction;
				}
		).collect( Collectors.toList());
	}

	public boolean isTrained()
	{
		return getTrainedSegmenters().findAny().isPresent();
	}

	private Stream< Segmenter > getTrainedSegmenters()
	{
		return segmenters().stream().map( SegmentationItem::segmenter ).filter( Segmenter::isTrained );
	}

}
