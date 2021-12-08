
package sc.fiji.labkit.ui.actions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.MenuBar;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.labeling.Labeling;
import sc.fiji.labkit.ui.labeling.LabelingSerializer;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements the "Import Labeling..." menu item.
 *
 * @author Matthias Arzt
 */
public class AddLabelingIoAction extends AbstractFileIoAction {

	private final Holder<Labeling> labeling;
	private final LabelingSerializer serializer;

	public AddLabelingIoAction(Extensible extensible, Holder<Labeling> labeling) {
		super(extensible, AbstractFileIoAction.LABELING_FILTER,
			AbstractFileIoAction.TIFF_FILTER);
		this.labeling = labeling;
		serializer = new LabelingSerializer(extensible.context());
		initOpenAction(MenuBar.LABELING_MENU, "Import Labeling ...", 100,
			this::openAdditional, "");
	}

	private void openAdditional(Void ignore, String filename) throws IOException {
		Labeling newLabeling = serializer.open(filename);
		extendLabeling(labeling.get(), newLabeling);
		labeling.notifier().notifyListeners();
	}

	// TODO: move to package Labeling
	private void extendLabeling(Labeling labeling, Labeling newLabeling) {
		newLabeling.iterableRegions().forEach((label, region) -> addLabel(labeling,
			label, region));
	}

	// TODO: move to package Labeling
	private void addLabel(Labeling labeling, Label label,
		IterableRegion<BitType> region)
	{
		String newLabelName = suggestName(label.name(), labeling.getLabels()
			.stream().map(Label::name).collect(Collectors.toList()));
		if (newLabelName == null) return;
		Label newLabel = labeling.addLabel(newLabelName);
		Cursor<Void> cursor = region.cursor();
		RandomAccess<LabelingType<Label>> ra = labeling.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			ra.setPosition(cursor);
			ra.get().add(newLabel);
		}
	}

	private String suggestName(String label, List<String> labels) {
		if (!labels.contains(label)) return label;
		for (int i = 0; i < 10000; i++) {
			String suggestion = label + i;
			if (!labels.contains(suggestion)) return suggestion;
		}
		return null;
	}
}
