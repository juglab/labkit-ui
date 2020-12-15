
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.holder.DefaultHolder;
import net.imglib2.labkit.utils.holder.Holder;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.util.Pair;
import org.scijava.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of {@link SegmentationItem}, with selected item, and a bunch of
 * listeners.
 */
public class SegmenterListModel {

	private final Context context;
	private final Holder<List<SegmentationItem>> segmenters = new DefaultHolder<>(new ArrayList<>());
	private final Holder<SegmentationItem> selectedSegmenter = new DefaultHolder<>(null);
	private final Holder<Boolean> segmentationVisibility = new DefaultHolder<>(true);
	private final Holder<List<Pair<ImgPlus<?>, Labeling>>> trainingData = new DefaultHolder<>(null);

	public SegmenterListModel(Context context) {
		this.context = context;
		this.segmenters.notifier().addListener(() -> {
			if (!segmenters.get().contains(selectedSegmenter.get())) selectedSegmenter.set(null);
		});
	}

	public Holder<List<SegmentationItem>> segmenters() {
		return segmenters;
	}

	public Holder<SegmentationItem> selectedSegmenter() {
		return selectedSegmenter;
	}

	public SegmentationItem addSegmenter(SegmentationPlugin plugin) {
		SegmentationItem segmentationItem = new SegmentationItem(plugin);
		segmenters.get().add(segmentationItem);
		segmenters.notifier().notifyListeners();
		return segmentationItem;
	}

	public void remove(SegmentationItem item) {
		segmenters.get().remove(item);
		segmenters.notifier().notifyListeners();
	}

	public Holder<Boolean> segmentationVisibility() {
		return segmentationVisibility;
	}

	public Context context() {
		return context;
	}

	public Holder<List<Pair<ImgPlus<?>, Labeling>>> trainingData() {
		return trainingData;
	}
}
