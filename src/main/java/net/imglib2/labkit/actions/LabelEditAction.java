package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.ColoredLabelsModel;

import javax.swing.*;

public class LabelEditAction {

	private final Extensible extensible;

	private final ColoredLabelsModel model;

	public LabelEditAction(Extensible extensible, boolean fixedLabels, ColoredLabelsModel model) {
		this.extensible = extensible;
		this.model = model;
		if(!fixedLabels) extensible.addLabelMenuItem("Rename", this::renameLabel, null);
		if(!fixedLabels) extensible.addLabelMenuItem("Move up", label -> model.moveLabel(label, -1), null);
		if(!fixedLabels) extensible.addLabelMenuItem("Move down", label -> model.moveLabel(label, 1), null);
		extensible.addLabelMenuItem("Clear", model::clearLabel, null);
		if(!fixedLabels) extensible.addLabelMenuItem("Remove", model::removeLabel, null);
	}

	private void renameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(extensible.dialogParent(),
				"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		model.renameLabel(label, newName);
	}
}
