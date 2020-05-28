
package net.imglib2.labkit.models;

import net.imglib2.labkit.segmentation.SegmentationPlugin;
import net.imglib2.labkit.utils.Notifier;
import org.scijava.Context;

import java.util.List;

public interface SegmenterListModel<T> {

	Context context();

	List<T> segmenters();

	Holder<T> selectedSegmenter();

	T addSegmenter(SegmentationPlugin segmenter);

	void remove(T item);

	Holder<Boolean> segmentationVisibility();

	Notifier listChangeListeners();
}
