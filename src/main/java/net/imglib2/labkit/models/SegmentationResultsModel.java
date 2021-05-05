
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.labkit.inputimage.ImgPlusViewsOld;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.labkit.utils.Notifier;
import net.imglib2.labkit.utils.DimensionUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.Arrays;
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
		int count = segmenter.classNames().size();
		ImgPlus<?> image = model.imageForSegmentation().get();
		int[] cellSize = segmenter.suggestCellSize(image);
		CellLoader<FloatType> loader = target -> segmenter.predict(image, ensureCellSize(segmenter,
			cellSize, target));
		Interval interval = intervalNoChannels(image);
		CellGrid grid = addDimensionToGrid(count, new CellGrid(Intervals.dimensionsAsLongArray(
			interval), cellSize));
		final ExtensionPoints.SetupCachedResultsImage predictionStorageFactory = extensionPoints
			.getPredictionStorageFactory();
		prediction = predictionStorageFactory.setupCachedImage(segmenter, loader, grid,
			new FloatType());
	}

	private CellGrid addDimensionToGrid(int size, CellGrid grid) {
		return new CellGrid(DimensionUtils.extend(grid
			.getImgDimensions(), size), DimensionUtils.extend(getCellDimensions(
				grid), size));
	}

	private void updateSegmentation(Segmenter segmenter) {
		ImgPlus<?> image = model.imageForSegmentation().get();
		int[] cellSize = segmenter.suggestCellSize(image);
		CellLoader<ShortType> loader = target -> segmenter.segment(image, ensureCellSize(segmenter,
			cellSize, target));
		Interval interval = intervalNoChannels(image);
		CellGrid grid = new CellGrid(Intervals.dimensionsAsLongArray(interval), cellSize);
		final ExtensionPoints.SetupCachedResultsImage segmentationStorageFactory = extensionPoints
			.getSegmentationStorageFactory();
		segmentation = segmentationStorageFactory.setupCachedImage(segmenter, loader, grid,
			new ShortType());
	}

	/**
	 * Grows the give target to cellSize if
	 * {@link Segmenter#requiresFixedCellSize()} is true.
	 */
	private <T extends NativeType<T> & NumericType<T>> RandomAccessibleInterval<T> ensureCellSize(
		Segmenter segmenter, int[] cellSize,
		RandomAccessibleInterval<T> target)
	{
		if (segmenter.requiresFixedCellSize()) {
			int[] targetSize = Intervals.dimensionsAsIntArray(target);
			if (!Arrays.equals(cellSize, targetSize)) {
				long[] min = Intervals.minAsLongArray(target);
				long[] max = new long[min.length];
				Arrays.setAll(max, d -> min[d] + cellSize[d] - 1);
				return Views.interval(Views.extendZero(target), min, max);
			}
		}
		return target;
	}

	private Interval intervalNoChannels(ImgPlus<?> image) {
		return new FinalInterval(ImgPlusViewsOld.hasAxis(image, Axes.CHANNEL) ? ImgPlusViewsOld
			.hyperSlice(image, Axes.CHANNEL, 0) : image);
	}

	private int[] getCellDimensions(CellGrid grid) {
		final int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
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
