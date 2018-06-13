
package net.imglib2.labkit.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class VisibilityPanel extends JPanel {

	public VisibilityPanel(ActionMap actions) {
		setLayout(new MigLayout("insets 0"));
		add(new JLabel("Visible Layers"), "wrap");
		addCheckbox(actions.get("Image"));
		addCheckbox(actions.get("Labeling"));
		addCheckbox(actions.get("Segmentation"));
	}

	private void addCheckbox(Action image) {
		JCheckBox checkbox = new JCheckBox(image);
		checkbox.setFocusable(false);
		add(checkbox, "wrap");
	}
}
