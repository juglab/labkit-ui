
package net.imglib2.labkit.models;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.segmentation.ForwardingSegmenter;
import net.imglib2.labkit.segmentation.Segmenter;
import net.imglib2.util.Pair;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SegmentationItem extends ForwardingSegmenter {

	public final static MenuKey<SegmentationItem> SEGMENTER_MENU = new MenuKey<>(
		SegmentationItem.class);

	private static final AtomicInteger counter = new AtomicInteger();

	private final String name = "Classifier-#" + counter.incrementAndGet();
	private final SegmentationResultsModel results;

	public SegmentationItem(SegmentationModel model, Segmenter segmenter) {
		super(segmenter);
		this.results = new SegmentationResultsModel(model, segmenter);
	}

	public String name() {
		return name;
	}

	public SegmentationResultsModel results() {
		return results;
	}

	@Override
	public String toString() {
		return name();
	}

	@Override
	public void openModel(String path) {
		super.openModel(path);
		results.update();
	}

	@Override
	public void train(
		List<Pair<? extends RandomAccessibleInterval<?>, ? extends Labeling>> data)
	{
		results.clear();
		super.train(data);
		results.update();
	}
}
