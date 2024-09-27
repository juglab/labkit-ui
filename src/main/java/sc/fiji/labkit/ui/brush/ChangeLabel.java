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

package sc.fiji.labkit.ui.brush;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

import sc.fiji.labkit.ui.LabKitKeymapManager;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.LabelingModel;

/**
 * {@link AbstractAction} that goes to the next label when "N" is pressed.
 */
public class ChangeLabel extends AbstractNamedAction {

	private static final String CHANGE_LABEL_ACTION = "next Label";
	private static final String[] CHANGE_LABEL_KEYS = new String[] { "N" };
	private static final String CHANGE_LABEL_DESCRIPTION = "Select the next label in the list.";

	private final LabelingModel model;

	public ChangeLabel(LabelingModel model) {
		super(CHANGE_LABEL_ACTION);
		this.model = model;
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("N"));
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		List<Label> labels = model.labeling().get().getLabels();
		Label nextLabel = next(labels, model.selectedLabel().get());
		if (nextLabel != null) model.selectedLabel().set(nextLabel);
	}

	private Label next(List<Label> labels, Label currentLabel) {
		if (labels.isEmpty()) return null;
		int index = labels.indexOf(currentLabel) + 1;
		if (index >= labels.size()) index = 0;
		return labels.get(index);
	}

	public void install(Actions actions) {
		actions.namedAction(this, CHANGE_LABEL_KEYS);
	}

	@Plugin(type = CommandDescriptionProvider.class)
	public static class Descriptions extends CommandDescriptionProvider {
		public Descriptions() {
			super(LabKitKeymapManager.LABKIT_SCOPE, LabKitKeymapManager.LABKIT_CONTEXT);
		}

		@Override
		public void getCommandDescriptions(final CommandDescriptions descriptions) {
			descriptions.add(CHANGE_LABEL_ACTION, CHANGE_LABEL_KEYS, CHANGE_LABEL_DESCRIPTION);
		}
	}
}
