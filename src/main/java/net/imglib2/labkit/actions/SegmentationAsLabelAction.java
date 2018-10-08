
package net.imglib2.labkit.actions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;
import java.util.Set;

/**
 * @author Matthias Arzt
 */
public class SegmentationAsLabelAction {

	private final Holder<Labeling> labelingHolder;
	private final Holder<SegmentationItem> selectedSegmenter;

	public SegmentationAsLabelAction(Extensible extensible,
		Holder<SegmentationItem> selectedSegmenter, Holder<Labeling> labelingHolder)
	{
		this.labelingHolder = labelingHolder;
		this.selectedSegmenter = selectedSegmenter;
		extensible.addAction("Create Label from Segmentation ...",
			"addSegmentationAsLabel", this::addSegmentationAsLabels, "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Create Label from Segmentation ...", this::addSegmentationAsLabel, null);
	}

	private void addSegmentationAsLabels() {
		addSegmentationAsLabel(selectedSegmenter.get());
	}

	private void addSegmentationAsLabel(SegmentationItem segmentationItem) {
		SegmentationResultsModel selectedResults = segmentationItem.results();
		List<String> labels = selectedResults.labels();
		String selected = (String) JOptionPane.showInputDialog(null,
			"Select label to be added", "Add Segmentation as Labels ...",
			JOptionPane.PLAIN_MESSAGE, null, labels.toArray(), labels.get(labels
				.size() - 1));
		int index = labels.indexOf(selected);
		if (index < 0) return;
		addLabel(selected, index, selectedResults.segmentation());
	}

	private void addLabel(String selected, int index,
		RandomAccessibleInterval<ShortType> segmentation)
	{
		Converter<ShortType, BitType> converter = (in, out) -> out.set(in
			.get() == index);
		RandomAccessibleInterval<BitType> result = Converters.convert(segmentation,
			converter, new BitType());
		addLabel(labelingHolder.get(), "segmented " + selected, result);
		labelingHolder.notifier().forEach(l -> l.accept(labelingHolder.get()));
	}

	// TODO move to better place
	private static void addLabel(Labeling labeling, String name,
		RandomAccessibleInterval<BitType> mask)
	{
		Cursor<BitType> cursor = Views.iterable(mask).cursor();
		Label label = labeling.addLabel(name);
		RandomAccess<Set<Label>> ra = labeling.randomAccess();
		while (cursor.hasNext()) {
			boolean value = cursor.next().get();
			if (value) {
				ra.setPosition(cursor);
				ra.get().add(label);
			}
		}
	}
}
