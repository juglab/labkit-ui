
package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.segmentation.weka.NewSegmenter;
import net.imglib2.labkit.segmentation.weka.TimeSeriesSegmenter;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.Context;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
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
	private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(
		true);

	public DefaultSegmentationModel(InputImage inputImage, Context context) {
		Labeling labeling = Labeling.createEmpty(Arrays.asList("background",
			"foreground"), inputImage.interval());
		this.imageLabelingModel = new ImageLabelingModel(inputImage.showable(),
			labeling, inputImage.isTimeSeries(), inputImage
				.getDefaultLabelingFilename());
		this.compatibleImage = inputImage.imageForSegmentation();
		this.grid = LabkitUtils.suggestGrid(inputImage.interval(),
			imageLabelingModel.isTimeSeries());
		this.segmenterFactory = () -> initClassifier(inputImage, context);
		this.selectedSegmenter = new DefaultHolder<>(addSegmenter());
	}

	private Segmenter initClassifier(InputImage inputImage, Context context) {
		Segmenter classifier1 =
			new NewSegmenter(context, inputImage);
		return inputImage.isTimeSeries() ? new TimeSeriesSegmenter(classifier1)
			: classifier1;
	}

	@Override
	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
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
	public void train(SegmentationItem item) {
		try {
			item.segmenter().train(Collections.singletonList(image()), Collections
				.singletonList(labeling()));
		}
		catch (CancellationException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Training Cancelled",
				JOptionPane.PLAIN_MESSAGE);
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.toString(), "Training Failed",
				JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void remove(SegmentationItem item) {
		if (segmenters.size() <= 1) return;
		segmenters.remove(item);
		if (!segmenters.contains(selectedSegmenter.get())) selectedSegmenter.set(
			segmenters.get(0));
	}

	@Override
	public void trainSegmenter() {
		train(selectedSegmenter().get());
	}

	@Override
	public Holder<Boolean> segmentationVisibility() {
		return segmentationVisibility;
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
