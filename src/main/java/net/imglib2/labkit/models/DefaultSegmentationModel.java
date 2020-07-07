
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.Context;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class DefaultSegmentationModel {

	private final Context context;
	private final ImageLabelingModel imageLabelingModel;
	private final SegmenterListModel segmenterList;

	public DefaultSegmentationModel(Context context, InputImage inputImage) {
		this.context = context;
		this.imageLabelingModel = new ImageLabelingModel(inputImage);
		this.segmenterList = new SegmenterListModel(context, Collections.singletonList(
			imageLabelingModel));
	}

	public DefaultSegmentationModel(Context context, ImageLabelingModel imageLabelingModel,
		SegmenterListModel segmenterList)
	{
		this.context = context;
		this.imageLabelingModel = imageLabelingModel;
		this.segmenterList = segmenterList;
	}

	public Context context() {
		return context;
	}

	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
	}

	public SegmenterListModel segmenterList() {
		return segmenterList;
	}

	public <T extends IntegerType<T> & NativeType<T>>
		List<RandomAccessibleInterval<T>> getSegmentations(T type)
	{
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation().get();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			RandomAccessibleInterval<T> labels = new CellImgFactory<>(type).create(
				image);
			segmenter.segment(image, labels);
			return labels;
		}).collect(Collectors.toList());
	}

	public List<RandomAccessibleInterval<FloatType>> getPredictions() {
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation().get();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			int numberOfClasses = segmenter.classNames().size();
			RandomAccessibleInterval<FloatType> prediction = new CellImgFactory<>(
				new FloatType()).create(DimensionUtils.appendDimensionToInterval(image,
					0, numberOfClasses - 1));
			segmenter.predict(image, prediction);
			return prediction;
		}).collect(Collectors.toList());
	}

	public boolean isTrained() {
		return getTrainedSegmenters().findAny().isPresent();
	}

	private Stream<Segmenter> getTrainedSegmenters() {
		return segmenterList.segmenters().stream().filter(Segmenter::isTrained).map(x -> x);
	}

}
