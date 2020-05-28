
package net.imglib2.labkit.actions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.models.SegmentationResultsModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;

/**
 * @author Matthias Arzt
 */
public class SegmentationAsLabelAction {

	private final Holder<Labeling> labelingHolder;
	private final Holder<? extends SegmentationItem> selectedSegmenter;

	public SegmentationAsLabelAction(
		Extensible extensible, DefaultSegmentationModel segmenationModel)
	{
		this.labelingHolder = segmenationModel.imageLabelingModel().labeling();
		this.selectedSegmenter = segmenationModel.selectedSegmenter();
		extensible.addMenuItem(MenuBar.SEGMENTER_MENU,
			"Create Label from Segmentation ...", 400,
			ignore -> ((Runnable) this::addSegmentationAsLabels).run(), null, "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Create Label from Segmentation ...", 400, this::addSegmentationAsLabel,
			null, null);
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
		labelingHolder.notifier().notifyListeners();
	}

	// TODO move to better place
	private static void addLabel(Labeling labeling, String name,
		RandomAccessibleInterval<BitType> mask)
	{
		Cursor<BitType> cursor = Views.iterable(mask).cursor();
		Label label = labeling.addLabel(name);
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		while (cursor.hasNext()) {
			boolean value = cursor.next().get();
			if (value) {
				ra.setPosition(cursor);
				ra.get().add(label);
			}
		}
	}
}
