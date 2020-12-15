
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.utils.properties.DefaultProperty;
import net.imglib2.labkit.utils.properties.Property;
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
	private final Property<List<SegmentationItem>> segmenters = new DefaultProperty<>(
		new ArrayList<>());
	private final Property<SegmentationItem> selectedSegmenter = new DefaultProperty<>(null);
	private final Property<Boolean> segmentationVisibility = new DefaultProperty<>(true);
	private final Property<List<Pair<ImgPlus<?>, Labeling>>> trainingData = new DefaultProperty<>(
		null);

	public SegmenterListModel(Context context) {
		this.context = context;
		this.segmenters.notifier().addListener(() -> {
			if (!segmenters.get().contains(selectedSegmenter.get())) selectedSegmenter.set(null);
		});
	}

	public Property<List<SegmentationItem>> segmenters() {
		return segmenters;
	}

	public Property<SegmentationItem> selectedSegmenter() {
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

	public Property<Boolean> segmentationVisibility() {
		return segmentationVisibility;
	}

	public Context context() {
		return context;
	}

	public Property<List<Pair<ImgPlus<?>, Labeling>>> trainingData() {
		return trainingData;
	}
}
