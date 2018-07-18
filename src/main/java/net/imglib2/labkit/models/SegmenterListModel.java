
package net.imglib2.labkit.models;

import java.util.List;

public interface SegmenterListModel<T> {

	List<T> segmenters();

	Holder<T> selectedSegmenter();

	T addSegmenter();

	void removeSelectedSegmenter();
}
