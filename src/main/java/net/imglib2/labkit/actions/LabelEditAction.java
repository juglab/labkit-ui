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
		if(!fixedLabels) extensible.addMenuItem( Label.LABEL_MENU, "Rename", 0, this::renameLabel, null,
				null);
		if(!fixedLabels) extensible.addMenuItem( Label.LABEL_MENU, "Move up", 100, label -> model.moveLabel(label, -1), null,
				null);
		if(!fixedLabels) extensible.addMenuItem(Label.LABEL_MENU, "Move down", 101, label -> model.moveLabel(label, 1), null,
				null);
		extensible.addMenuItem( Label.LABEL_MENU, "Clear", 200, model::clearLabel, null,
				null);
		if(!fixedLabels) extensible.addMenuItem( Label.LABEL_MENU, "Remove", 201, model::removeLabel, null,
				null);
	}

	private void renameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(extensible.dialogParent(),
				"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		model.renameLabel(label, newName);
	}
}
