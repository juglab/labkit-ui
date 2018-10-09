
package net.imglib2.labkit.actions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.MenuBar;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Matthias Arzt
 */
public class AddLabelingIoAction extends AbstractFileIoAction {

	private final Holder<Labeling> labeling;
	private final LabelingSerializer serializer;

	public AddLabelingIoAction(Extensible extensible, Holder<Labeling> labeling) {
		super(extensible, new FileNameExtensionFilter("Labeling (*.labeling)",
			"labeling"));
		this.labeling = labeling;
		serializer = new LabelingSerializer(extensible.context());
		initOpenAction(MenuBar.LABELING_MENU, "Import Labeling ...", 100,
				this::openAdditional, "");
	}

	private void openAdditional(String filename) throws IOException {
		Labeling newLabeling = serializer.open(filename);
		extendLabeling(labeling.get(), newLabeling);
		labeling.notifier().forEach(listener -> listener.accept(labeling.get()));
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
		RandomAccess<Set<Label>> ra = labeling.randomAccess();
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
