/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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

import sc.fiji.labkit.ui.Extensible;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;

import javax.swing.*;

/**
 * Implements menu items for renaming and removing individual labels. Also
 * allows to change the order of the labels.
 */
public class LabelEditAction {

	private final Extensible extensible;

	private final ColoredLabelsModel model;

	public LabelEditAction(Extensible extensible, boolean fixedLabels,
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
	}

	private void renameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(extensible.dialogParent(),
			"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		model.renameLabel(label, newName);
	}
}
