
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.inputimage.InputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.scijava.Context;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Serves as a model for PredictionLayer and TrainClassifierAction
 */
public class DefaultSegmentationModel implements SegmenterListModel<SegmentationItem> {

	private final Context context;
	private final ImageLabelingModel imageLabelingModel;
	private final Holder<SegmentationItem> selectedSegmenter;
	private final List<SegmentationItem> segmenters = new ArrayList<>();
	private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(
		true);
	private final Notifier listeners = new Notifier();

	public DefaultSegmentationModel(InputImage inputImage, Context context) {
		this.context = context;
		this.imageLabelingModel = new ImageLabelingModel(inputImage);
		this.selectedSegmenter = new DefaultHolder<>(null);
	}

	public Context context() {
		return context;
	}

	public ImageLabelingModel imageLabelingModel() {
		return imageLabelingModel;
	}

	@Override
	public List<SegmentationItem> segmenters() {
		return Collections.unmodifiableList(segmenters);
	}

	@Override
	public Holder<SegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	@Override
	public SegmentationItem addSegmenter(SegmentationPlugin plugin) {
		SegmentationItem segmentationItem = new SegmentationItem(this.imageLabelingModel(), plugin);
		segmenters.add(segmentationItem);
		listeners.notifyListeners();
		return segmentationItem;
	}

	@Override
	public void remove(SegmentationItem item) {
		segmenters.remove(item);
		if (!segmenters.contains(selectedSegmenter.get())) selectedSegmenter.set(null);
		listeners.notifyListeners();
	}

	@Override
	public Holder<Boolean> segmentationVisibility() {
		return segmentationVisibility;
	}

	@Override
	public Notifier listChangeListeners() {
		return listeners;
	}

	public <T extends IntegerType<T> & NativeType<T>>
		List<RandomAccessibleInterval<T>> getSegmentations(T type)
	{
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation();
		Stream<Segmenter> trainedSegmenters = getTrainedSegmenters();
		return trainedSegmenters.map(segmenter -> {
			RandomAccessibleInterval<T> labels = new CellImgFactory<>(type).create(
				image);
			segmenter.segment(image, labels);
			return labels;
		}).collect(Collectors.toList());
	}

	public List<RandomAccessibleInterval<FloatType>> getPredictions() {
		ImgPlus<?> image = imageLabelingModel().imageForSegmentation();
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
		return segmenters().stream().filter(Segmenter::isTrained).map(x -> x);
	}

}
