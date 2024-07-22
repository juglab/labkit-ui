/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2024 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.actions;

import javax.swing.JOptionPane;

import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.Actions;

import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.LabKitKeymapManager;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;

/**
 * Implements menu items for renaming and removing individual labels. Also
 * allows to change the order of the labels.
 */
public class LabelEditAction {


	private final Extensible extensible;

	private final ColoredLabelsModel model;

	public LabelEditAction(Extensible extensible, boolean fixedLabels, ColoredLabelsModel model) {
		this(null, extensible, fixedLabels, model);
	}

	public LabelEditAction(Actions actions, Extensible extensible, boolean fixedLabels,
		ColoredLabelsModel model)
	{
		this.extensible = extensible;
		this.model = model;
		if (!fixedLabels) extensible.addMenuItem(Label.LABEL_MENU, "Rename", 0,
			this::renameLabel, null, null);
		if (!fixedLabels) extensible.addMenuItem(Label.LABEL_MENU, "Move up", 100,
			label -> model.moveLabel(label, -1), null, null);
		if (!fixedLabels) extensible.addMenuItem(Label.LABEL_MENU, "Move down", 101,
			label -> model.moveLabel(label, 1), null, null);
		extensible.addMenuItem(Label.LABEL_MENU, "Clear", 200, model::clearLabel,
			null, null);
		if (!fixedLabels) extensible.addMenuItem(Label.LABEL_MENU, "Remove", 201,
			model::removeLabel, null, null);

		// Actions.
		if (actions != null) {
			if (!fixedLabels) {
				actions.runnableAction(() -> renameLabel(model.selected().get()), RENAME_LABEL_ACTION,
						RENAME_LABEL_KEYS);
				actions.runnableAction(() -> model.moveLabel(model.selected().get(), -1), MOVE_LABEL_UP_ACTION,
						MOVE_LABEL_UP_KEYS);
				actions.runnableAction(() -> model.moveLabel(model.selected().get(), 1), MOVE_LABEL_DOWN_ACTION,
						MOVE_LABEL_DOWN_KEYS);
				actions.runnableAction(() -> model.removeLabel(model.selected().get()), REMOVE_LABEL_ACTION,
						REMOVE_LABEL_KEYS);
			}
			actions.runnableAction(() -> model.clearLabel(model.selected().get()), CLEAR_LABEL_ACTION,
					CLEAR_LABEL_KEYS);
		}

	}

	private void renameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(extensible.dialogParent(),
			"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		model.renameLabel(label, newName);
	}

	@Plugin(type = CommandDescriptionProvider.class)
	public static class Descriptions extends CommandDescriptionProvider {

		public Descriptions() {
			super(LabKitKeymapManager.LABKIT_SCOPE, LabKitKeymapManager.LABKIT_CONTEXT);
		}

		@Override
		public void getCommandDescriptions(final CommandDescriptions descriptions) {
			descriptions.add(RENAME_LABEL_ACTION, RENAME_LABEL_KEYS, RENAME_LABEL_DESCRIPTION);
			descriptions.add(MOVE_LABEL_UP_ACTION, MOVE_LABEL_UP_KEYS, MOVE_LABEL_UP_DESCRIPTION);
			descriptions.add(MOVE_LABEL_DOWN_ACTION, MOVE_LABEL_DOWN_KEYS, MOVE_LABEL_DOWN_DESCRIPTION);
			descriptions.add(REMOVE_LABEL_ACTION, REMOVE_LABEL_KEYS, REMOVE_LABEL_DESCRIPTION);
			descriptions.add(CLEAR_LABEL_ACTION, CLEAR_LABEL_KEYS, CLEAR_LABEL_DESCRIPTION);
		}
	}

	private static final String RENAME_LABEL_ACTION = "rename current label";
	private static final String MOVE_LABEL_UP_ACTION = "move selected label up";
	private static final String MOVE_LABEL_DOWN_ACTION = "move selected label down";
	private static final String REMOVE_LABEL_ACTION = "remove selected label";
	private static final String CLEAR_LABEL_ACTION = "clear selected label";

	private static final String[] RENAME_LABEL_KEYS = new String[] { "not mapped" };
	private static final String[] MOVE_LABEL_UP_KEYS = new String[] { "not mapped" };
	private static final String[] MOVE_LABEL_DOWN_KEYS = new String[] { "not mapped" };
	private static final String[] REMOVE_LABEL_KEYS = new String[] { "not mapped" };
	private static final String[] CLEAR_LABEL_KEYS = new String[] { "not mapped" };

	private static final String RENAME_LABEL_DESCRIPTION = "Rename the label currently selected.";
	private static final String MOVE_LABEL_UP_DESCRIPTION = "Move the label currently selected up in the list.";
	private static final String MOVE_LABEL_DOWN_DESCRIPTION = "Move the label currently selected down in the list.";
	private static final String REMOVE_LABEL_DESCRIPTION = "Remove the label currently selected.";
	private static final String CLEAR_LABEL_DESCRIPTION = "Clear the annotations for the label currently selected.";



}
