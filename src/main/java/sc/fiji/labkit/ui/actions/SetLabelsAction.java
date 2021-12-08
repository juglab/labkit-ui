
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
