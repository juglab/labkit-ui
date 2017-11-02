package net.imglib2.atlas.actions;

import net.imglib2.Interval;
import net.imglib2.atlas.MainFrame;
import net.imglib2.atlas.Preferences;
import net.imglib2.atlas.labeling.Labeling;
import org.scijava.prefs.PrefService;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class SetLabelsAction {

	private final Preferences preference;
	private final MainFrame.Extensible extensible;

	public SetLabelsAction(MainFrame.Extensible extensible, Preferences preferences) {
		this.extensible = extensible;
		this.preference = preferences;
		extensible.addAction("Change Available Labels ...", "changeLabels", this::changeLabels, "");
		extensible.addAction("Default Available Labels ...", "defaultLabels", this::defaultLabels, "");
		extensible.addAction("Delete All Labels", "clearLabeling", this::changeLabels, "");
	}

	private void changeLabels() {
		Labeling labeling = extensible.getLabeling();
		List<String> labels = labeling.getLabels();
		Optional<List<String>> results = dialog(labels);
		if(results.isPresent())
			extensible.setLabeling(new Labeling(results.get(), (Interval) labeling));
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
}
