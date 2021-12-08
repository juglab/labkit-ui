
package sc.fiji.labkit.ui.actions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.models.SegmentationItem;
import sc.fiji.labkit.ui.models.SegmentationModel;
import sc.fiji.labkit.ui.models.SegmentationResultsModel;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.util.List;

/**
 * Implements the create label form segmentation menu item.
 *
 * @author Matthias Arzt
 */
public class SegmentationAsLabelAction {

	private final ImageLabelingModel labelingModel;
	private final Holder<? extends SegmentationItem> selectedSegmenter;

	public SegmentationAsLabelAction(
		Extensible extensible, SegmentationModel segmentationModel)
	{
		this.labelingModel = segmentationModel.imageLabelingModel();
		this.selectedSegmenter = segmentationModel.segmenterList().selectedSegmenter();
		extensible.addMenuItem(MenuBar.SEGMENTER_MENU,
			"Create Label from Segmentation ...", 400,
			ignore -> addSegmentationAsLabels(), null, "");
		extensible.addMenuItem(SegmentationItem.SEGMENTER_MENU,
			"Create Label from Segmentation ...", 400, this::addSegmentationAsLabel,
			null, null);
	}

	private void addSegmentationAsLabels() {
		addSegmentationAsLabel(selectedSegmenter.get());
	}

	private void addSegmentationAsLabel(SegmentationItem segmentationItem) {
		SegmentationResultsModel selectedResults = segmentationItem.results(labelingModel);
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
		Holder<Labeling> labelingHolder = labelingModel.labeling();
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
