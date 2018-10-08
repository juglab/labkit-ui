
package net.imglib2.labkit.models;

import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.segmentation.Segmenter;

import java.util.concurrent.atomic.AtomicInteger;

public class SegmentationItem {

	public final static MenuKey<SegmentationItem> SEGMENTER_MENU =
			new MenuKey<>(SegmentationItem.class);

	private static final AtomicInteger counter = new AtomicInteger();

	private final String name = "Classifier-#" + counter.incrementAndGet();
	private final Segmenter segmenter;
	private final SegmentationResultsModel results;

	public SegmentationItem(SegmentationModel model, Segmenter segmenter) {
		this.segmenter = segmenter;
		this.results = new SegmentationResultsModel(model, segmenter);
	}

	public Segmenter segmenter() {
		return segmenter;
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
}
