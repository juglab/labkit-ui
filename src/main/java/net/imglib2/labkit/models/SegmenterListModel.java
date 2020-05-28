
package net.imglib2.labkit.models;

import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.imglib2.labkit.utils.Notifier;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SegmenterListModel {

	private final Context context;
	private final List<ImageLabelingModel> imageLabelingModels;
	private final List<SegmentationItem> segmenters = new ArrayList<>();
	protected final Holder<SegmentationItem> selectedSegmenter = new DefaultHolder<>(null);
	private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(true);
	private final Notifier listeners = new Notifier();

	public SegmenterListModel(Context context, List<ImageLabelingModel> imageLabelingModels) {
		this.context = context;
		this.imageLabelingModels = imageLabelingModels;
	}

	public List<SegmentationItem> segmenters() {
		return Collections.unmodifiableList(segmenters);
	}

	public Holder<SegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	public SegmentationItem addSegmenter(SegmentationPlugin plugin) {
		SegmentationItem segmentationItem = new SegmentationItem(imageLabelingModels.get(0), plugin);
		segmenters.add(segmentationItem);
		listeners.notifyListeners();
		return segmentationItem;
	}

	public void remove(SegmentationItem item) {
		segmenters.remove(item);
		if (!segmenters.contains(selectedSegmenter.get())) selectedSegmenter.set(null);
		listeners.notifyListeners();
	}

	public Holder<Boolean> segmentationVisibility() {
		return segmentationVisibility;
	}

	public Notifier listChangeListeners() {
		return listeners;
	}

	public Context context() {
		return context;
	}

	public void train(SegmentationItem item) {
		TrainClassifier.train(imageLabelingModels, item);
	}
}
