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

import sc.fiji.labkit.ui.Preferences;
import sc.fiji.labkit.ui.SegmentationComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

// FIXME
public class SetLabelsAction {

	private final Preferences preference;
	private final SegmentationComponent segmentationComponent;

	public SetLabelsAction(SegmentationComponent segmentationComponent,
		Preferences preferences)
	{
		// TODO: clean this mess up
		this.segmentationComponent = segmentationComponent;
		this.preference = preferences;
		addAction("Available Labels On Start Up ...", "defaultLabels",
			this::defaultLabels);
	}

	private void addAction(String title, String command, Runnable action) {
		// RunnableAction a = new RunnableAction(title, action);
		// a.putValue(Action.ACTION_COMMAND_KEY, command);
		// segmentationComponent.getActions(key).put(command, a);
	}

	private void defaultLabels() {
		Optional<List<String>> result = dialog(preference.getDefaultLabels());
		result.ifPresent(preference::setDefaultLabels);
	}

	private static Optional<List<String>> dialog(List<String> labels) {
		JTextArea textArea = new JTextArea();
		StringJoiner joiner = new StringJoiner("\n");
		labels.forEach(joiner::add);
		textArea.setPreferredSize(new Dimension(200, 200));
		textArea.setText(joiner.toString());
		int result = JOptionPane.showConfirmDialog(null, new Object[] {
			"When Labkit is started the first time for an image,\nthese labels will be available:",
			new JScrollPane(textArea) }, "Labels On Start Up",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) return Optional.of(Arrays.asList(
			textArea.getText().split("\n")));
		else return Optional.empty();
	}

	public static void main(String... args) {
		Optional<List<String>> result = SetLabelsAction.dialog(Arrays.asList(
			"Hello", "World"));
		System.out.println(result);
	}
}
