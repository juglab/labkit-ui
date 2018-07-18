
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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class DefaultSegmentationModel implements SegmentationModel,
	SegmenterListModel<SegmentationItem>
{

	private final ImageLabelingModel imageLabelingModel;
	private final Holder<SegmentationItem> selectedSegmenter;
	private final Supplier<Segmenter> segmenterFactory;
	private List<SegmentationItem> segmenters = new ArrayList<>();
	private final RandomAccessibleInterval<?> compatibleImage;
	private final CellGrid grid;

	public DefaultSegmentationModel(RandomAccessibleInterval<?> compatibleImage,
		ImageLabelingModel imageLabelingModel, Supplier<Segmenter> segmenterFactory)
	{
		this.imageLabelingModel = imageLabelingModel;
		this.compatibleImage = compatibleImage;
		this.grid = LabkitUtils.suggestGrid(this.compatibleImage, imageLabelingModel
			.isTimeSeries());
		this.segmenterFactory = segmenterFactory;
		this.selectedSegmenter = new DefaultHolder<>(addSegmenter());
	}

	@Override
	public Labeling labeling() {
		return imageLabelingModel.labeling().get();
	}

	@Override
	public RandomAccessibleInterval<?> image() {
		return compatibleImage;
	}

	@Override
	public CellGrid grid() {
		return grid;
	}

	@Override
	public List<SegmentationItem> segmenters() {
		return segmenters;
	}

	@Override
	public Holder<SegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	@Override
	public ColorMap colorMap() {
		return imageLabelingModel.colorMapProvider().colorMap();
	}

	@Override
	public AffineTransform3D labelTransformation() {
		return imageLabelingModel.labelTransformation();
	}

	@Override
	public SegmentationItem addSegmenter() {
		SegmentationItem segmentationItem = new SegmentationItem(this,
			segmenterFactory.get());
		this.segmenters.add(segmentationItem);
		return segmentationItem;
	}

	@Override
	public void removeSelectedSegmenter() {
		if(segmenters.size() <= 1)
			return;
		segmenters.remove(selectedSegmenter.get());
	}

	@Override
	public void trainSegmenter() {
		selectedSegmenter().get().segmenter().train(Collections.singletonList(
			image()), Collections.singletonList(labeling()));
	}

	public <T extends IntegerType<T> & NativeType<T>>
		List<RandomAccessibleInterval<T>> getSegmentations(T type)
	{
		RandomAccessibleInterval<?> image = image();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			RandomAccessibleInterval<T> labels = new CellImgFactory<>(type).create(
				image);
			segmenter.segment(image, labels);
			return labels;
		}).collect(Collectors.toList());
	}

	public List<RandomAccessibleInterval<FloatType>> getPredictions() {
		RandomAccessibleInterval<?> image = image();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			int numberOfClasses = segmenter.classNames().size();
			RandomAccessibleInterval<FloatType> prediction = new CellImgFactory<>(
				new FloatType()).create(RevampUtils.appendDimensionToInterval(image, 0,
					numberOfClasses - 1));
			segmenter.predict(image, prediction);
			return prediction;
		}).collect(Collectors.toList());
	}

	public boolean isTrained() {
		return getTrainedSegmenters().findAny().isPresent();
	}

	private Stream<Segmenter> getTrainedSegmenters() {
		return segmenters().stream().map(SegmentationItem::segmenter).filter(
			Segmenter::isTrained);
	}

}
