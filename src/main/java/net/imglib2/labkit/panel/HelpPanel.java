package net.imglib2.labkit.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class HelpPanel extends JPanel {

	public HelpPanel() {
		setLayout(new MigLayout());
		add(initHelpButton("Draw (D)",
				"To draw a label: Press \"D\" on the keyboard,\n" +
						"(keep it pressed) and click with the mouse on the image."));
		add(initHelpButton("Erase (E)",
				"To erase a label: Press \"E\" on the keyboard,\n" +
						"(keep it pressed) and click with the mouse on the image."));
		add(initHelpButton("Flood Fill (F)",
				"This only works properly on 2D images,\n" +
						"Draw a circle on the image. Press \"F\" on the keyboard,\n" +
						"(keep it pressed) and click the inner of the circle."));
		add(initHelpButton("Flood Remove (R)",
				"To remove one connected component of a label: Press \"R\" on the keyboard,\n" +
						"(keep it pressed) and click the inner of the circle."));
		add(initHelpButton("Next Label (N)",
				"To switch between labels: Press N on the keyboard,\n" +
						"or select the label on the left panel."));
	}

	private JButton initHelpButton(String buttonTitle, String helpText) {
		JButton button = new JButton(buttonTitle);
		button.addActionListener(l -> showMessage(buttonTitle, helpText));
		return button;
	}

	private void showMessage(String text, String explain) {
		JOptionPane.showMessageDialog(null, explain, text, JOptionPane.INFORMATION_MESSAGE);
	}
}
