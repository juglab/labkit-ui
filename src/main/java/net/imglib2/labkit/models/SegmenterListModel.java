
package net.imglib2.labkit.models;

import java.util.List;

public interface SegmenterListModel<T> {

	List<T> segmenters();

	Holder<T> selectedSegmenter();

	T addSegmenter();

	void train(T item);

	void remove(T item);
}
