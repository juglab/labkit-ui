package net.imglib2.atlas.actions;

import net.imglib2.Interval;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.Preferences;
import net.imglib2.atlas.labeling.Labeling;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SetLabelsAction {

	private final Preferences preference;
	private final JMenu menu = new JMenu("Labels");
	private final Supplier<Labeling> getLabeling;
	private final Consumer<Labeling> setLabeling;

	public SetLabelsAction(Supplier<Labeling> getLabeling, Consumer<Labeling> setLabeling, Preferences preferences) {
		// TODO: clean this mess up
		this.getLabeling = getLabeling;
		this.setLabeling = setLabeling;
		this.preference = preferences;
		addAction("Change Available Labels ...", "changeLabels", this::changeLabels);
		addAction("Default Available Labels ...", "defaultLabels", this::defaultLabels);
		addAction("Delete All Labels", "clearLabeling", this::clearLabels);
	}

	private void addAction(String title, String command, Runnable action) {
		RunnableAction a = new RunnableAction(title, action);
		a.putValue(Action.ACTION_COMMAND_KEY, command);
		menu.add(a);
	}

	private void changeLabels() {
		Labeling labeling = getLabeling.get();
		List<String> labels = labeling.getLabels();
		Optional<List<String>> results = dialog(labels);
		if(results.isPresent()) {
			Labeling newLabeling = new Labeling(results.get(), (Interval) labeling);
			newLabeling.setAxes(labeling.axes());
			setLabeling.accept(newLabeling);
		}
	}

	private void clearLabels() {
		Labeling oldLabeling = getLabeling.get();
		Labeling newLabeling = new Labeling(oldLabeling.getLabels(), (Interval) oldLabeling);
		newLabeling.setAxes(oldLabeling.axes());
		setLabeling.accept(newLabeling);
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
		int result = JOptionPane.showConfirmDialog(null,
				new Object[]{"Please input new labels:", new JScrollPane(textArea)},
				"Change Available Labels",
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if(result == JOptionPane.OK_OPTION)
			return Optional.of(Arrays.asList(textArea.getText().split("\n")));
		else
			return Optional.empty();
	}

	public static void main(String... args) {
		Optional<List<String>> result = SetLabelsAction.dialog(Arrays.asList("Hello", "World"));
		System.out.println(result);
	}

	public JMenu getMenu() {
		return menu;
	}
}
