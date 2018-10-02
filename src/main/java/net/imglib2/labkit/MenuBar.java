
package net.imglib2.labkit;

import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Matthias Arzt
 */
public class MenuBar extends JMenuBar {

	private Map<String, Action> actions = new TreeMap<>();

	MenuBar(ActionMap actions) {
		for (Object key : actions.keys()) {
			Action action = actions.get(key);
			String command = (String) action.getValue(Action.ACTION_COMMAND_KEY);
			if (command != null) this.actions.put(command, action);
		}

		setupSortedMenu();
		setupOthers();
	}

	private void setupSortedMenu() {
		addMenu("Labeling").addItem("openLabeling").addItem("saveLabeling").addItem(
			"showLabeling").addSeparator().addItem("importLabel").addItem(
				"openAdditionalLabeling").addItem("exportLabel");
		addMenu("Segmentation").addItem("trainClassifier").addItem(
			"segmenterSettings").addItem("saveClassifier").addItem("openClassifier")
			.addSeparator().addItem("showSegmentation").addItem("saveSegmentation")
			.addItem("showPrediction").addItem("savePrediction").addSeparator()
			.addItem("addSegmentationAsLabel");
		addMenu("View").addCheckBox("toggleImage").addCheckBox("toggleLabeling")
			.addCheckBox("toggleSegmentation").addSeparator().addItem("resetView");
	}

	private MenuBuilder addMenu(String title) {
		JMenu menu = new JMenu(title);
		add(menu);
		return new MenuBuilder(menu);
	}

	private Action getAction(String command) {
		Action action = actions.get(command);
		if (action == null) return new RunnableAction("Action not found: " +
			command, () -> {});
		actions.remove(command);
		return action;
	}

	private void setupOthers() {
		MenuBuilder menu = addMenu("Others");
		actions.forEach((ignore, action) -> menu.addItem(action));
	}

	private class MenuBuilder {

		private final JMenu menu;

		public MenuBuilder(JMenu menu) {
			this.menu = menu;
		}

		public MenuBuilder addItem(String command) {
			return addItem(getAction(command));
		}

		private MenuBuilder addItem(Action action) {
			menu.add(new JMenuItem(action));
			return this;
		}

		public MenuBuilder addCheckBox(String command) {
			menu.add(new JCheckBoxMenuItem(getAction(command)));
			return this;
		}

		public MenuBuilder addSeparator() {
			menu.addSeparator();
			return this;
		}
	}
}
