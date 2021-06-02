
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.SegmentationUtils;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * SegmentationResultsModel is segmentation + probability map. It and updates
 * whenever the Segmentation changes. It's possible to listen to the
 * SegmentationResultsModel.
 */
public class SegmentationResultsModel {

	private final ExtensionPoints extensionPoints;
	private final ImageLabelingModel model;
	private final Segmenter segmenter;
	private boolean hasResults = false;
	private RandomAccessibleInterval<ShortType> segmentation;
	private RandomAccessibleInterval<FloatType> prediction;
	private List<String> labels = Collections.emptyList();
	private List<ARGBType> colors = Collections.emptyList();

	private final Notifier listeners = new Notifier();

	public SegmentationResultsModel(ImageLabelingModel model, ExtensionPoints extensionPoints,
		Segmenter segmenter)
	{
		this.model = model;
		this.extensionPoints = extensionPoints;
		this.segmenter = segmenter;
		segmentation = dummy(new ShortType());
		prediction = dummy(new FloatType());
		model.imageForSegmentation().notifier().addListener(this::update);
		update();
	}

	public ImageLabelingModel imageLabelingModel() {
		return model;
	}

	public void update() {
		if (segmenter.isTrained()) {
			updateSegmentation(segmenter);
			updatePrediction(segmenter);
			this.labels = segmenter.classNames();
			this.colors = this.labels.stream().map(this::getLabelColor).collect(
				Collectors.toList());
			hasResults = true;
			listeners.notifyListeners();
		}
	}

	private ARGBType getLabelColor(String name) {
		Labeling labeling = model.labeling().get();
		try {
			return labeling.getLabel(name).color();
		}
		catch (NoSuchElementException e) {
			return labeling.addLabel(name).color();
		}
	}

	public void clear() {
		segmentation = dummy(new ShortType());
		prediction = dummy(new FloatType());
		hasResults = false;
		listeners.notifyListeners();
	}

	public RandomAccessibleInterval<ShortType> segmentation() {
		return segmentation;
	}

	private <T> RandomAccessibleInterval<T> dummy(T value) {
		FinalInterval interval = new FinalInterval(model.imageForSegmentation().get());
		return ConstantUtils.constantRandomAccessibleInterval(value, interval);
	}

	public RandomAccessibleInterval<FloatType> prediction() {
		return prediction;
	}

	private void updatePrediction(Segmenter segmenter) {
		ImgPlus<?> image = model.imageForSegmentation().get();
		this.prediction = SegmentationUtils.createCachedProbabilityMap(segmenter, image,
			extensionPoints.getCachedPredictionImageFactory());
	}

	private void updateSegmentation(Segmenter segmenter) {
		ImgPlus<?> image = model.imageForSegmentation().get();
		this.segmentation = SegmentationUtils.createCachedSegmentation(segmenter, image,
			extensionPoints.getCachedSegmentationImageFactory());
	}

	public List<String> labels() {
		return labels;
	}

	public Interval interval() {
		return new FinalInterval(Intervals.dimensionsAsLongArray(model.imageForSegmentation().get()));
	}

	public List<ARGBType> colors() {
		return colors;
	}

	public Notifier segmentationChangedListeners() {
		return listeners;
	}

	public boolean hasResults() {
		return hasResults;
	}
}
