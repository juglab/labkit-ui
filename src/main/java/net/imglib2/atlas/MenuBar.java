package net.imglib2.atlas;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * @author Matthias Arzt
 */
public class MenuBar extends JMenuBar {

	private Map<String, Action> actions = new TreeMap<>();

	MenuBar(ActionMap actions) {
		for (Object key : actions.keys()) {
			Action action = actions.get(key);
			String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
			if(command != null)
				this.actions.put(command, action);
		}

		setupSortedMenu();
		setupOthers();
	}

	private void setupSortedMenu() {
		addMenu("Labels", "loadLabeling", "saveLabeling", "changeLabels", "clearLabeling");
		addMenu("Classifier",
				"trainClassifier",
				"saveClassifier",
				"loadClassifier",
				"showSegmentation",
				"saveSegmentation",
				"batchSegment",
				"changeFeatures",
				"selectAlgorithm");
	}

	private void addMenu(String title, String... actionCommandKeys) {
		JMenu menu = new JMenu(title);
		for(String command : actionCommandKeys)
			menu.add(getItem(command));
		add(menu);
	}

	private JMenuItem getItem(String command) {
		Action action = actions.get(command);
		if(action == null)
			return new JMenuItem("Action not found: " + command);
		actions.remove(command);
		return new JMenuItem(action);
	}

	private void setupOthers() {
		JMenu menu = new JMenu("Others");
		actions.forEach((ignore, action) -> menu.add(action));
		add(menu);
	}
}
