package net.imglib2.labkit.actions;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.Holder;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.labeling.LabelingSerializer;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Matthias Arzt
 */
public class AddLabelingIoAction extends AbstractFileIoAcion {

	private final Holder<Labeling> labeling;
	private final LabelingSerializer serializer;

	public AddLabelingIoAction(Extensible extensible, Holder<Labeling> labeling) {
		super(extensible, new FileNameExtensionFilter("Labeling (*.labeling)", "labeling"));
		this.labeling = labeling;
		serializer = new LabelingSerializer(extensible.context());
		initOpenAction("Open additional Labeling ...", "openAdditionalLabeling", this::openAdditional, "");
	}

	private void openAdditional(String filename) throws IOException {
		Labeling newLabeling = serializer.open(filename);
		extendLabeling(labeling.get(), newLabeling);
		labeling.notifier().forEach(listener -> listener.accept(labeling.get()));
	}

	// TODO: move to package Labeling
	private void extendLabeling(Labeling labeling, Labeling newLabeling) {
		newLabeling.iterableRegions().forEach((label, region) -> addLabel(labeling, label, region));
	}

	// TODO: move to package Labeling
	private void addLabel(Labeling labeling, String label, IterableRegion<BitType> region) {
		String newLabel = suggestName(label, labeling.getLabels());
		if(newLabel == null)
			return;
		labeling.addLabel(newLabel);
		Cursor<Void> cursor = region.cursor();
		RandomAccess<Set<String>> ra = labeling.randomAccess();
		while(cursor.hasNext()) {
			cursor.fwd();
			ra.setPosition(cursor);
			ra.get().add(label);
		}
	}

	private String suggestName(String label, List<String> labels) {
		if(!labels.contains(label))
			return label;
		for (int i = 0; i < 10000; i++) {
			String suggestion = label + i;
			if(!labels.contains(suggestion))
				return suggestion;
		}
		return null;
	}
}
