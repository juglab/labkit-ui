
package net.imglib2.labkit.models;

import net.imagej.ImgPlus;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.segmentation.ForwardingSegmenter;
import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SegmentationItem extends ForwardingSegmenter {

	public final static MenuKey<SegmentationItem> SEGMENTER_MENU = new MenuKey<>(
		SegmentationItem.class);

	private static final AtomicInteger counter = new AtomicInteger();

	private final String name;

	private String filename;

	private boolean modified;

	private final Map<ImageLabelingModel, SegmentationResultsModel> results;

	public SegmentationItem(ImageLabelingModel model, SegmentationPlugin plugin) {
		super(plugin.createSegmenter(model.imageForSegmentation().get()));
		this.name = "#" + counter.incrementAndGet() + " - " + plugin.getTitle();
		this.results = new HashMap<>();
		this.filename = null;
		this.modified = false;
	}

	@Deprecated
	public Segmenter segmenter() {
		return this;
	}

	public String name() {
		return name;
	}

	public SegmentationResultsModel results(ImageLabelingModel imageLabeling) {
		SegmentationResultsModel result = results.get(imageLabeling);
		if (result == null) {
			result = new SegmentationResultsModel(imageLabeling, getSourceSegmenter());
			results.put(imageLabeling, result);
		}
		return result;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public void openModel(String path) {
		super.openModel(path);
		filename = path;
		modified = false;
		results.forEach((i, r) -> r.update());
	}

	@Override
	public void train(List<Pair<ImgPlus<?>, Labeling>> data) {
		results.forEach((i, r) -> r.clear());
		modified = true;
		super.train(data);
		results.forEach((i, r) -> r.update());
	}

	public boolean isModified() {
		return modified;
	}

	public String getFileName() {
		return filename;
	}
}
