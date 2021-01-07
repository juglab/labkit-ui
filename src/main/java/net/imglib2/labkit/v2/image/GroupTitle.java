
package net.imglib2.labkit.v2.image;

import net.imglib2.labkit.panel.GuiUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class GroupTitle extends JPanel {

	private final JCheckBox checkbox;

	private final JLabel label;

	public GroupTitle(String title) {
		setLayout(new BorderLayout());
		setBackground(new Color(200, 200, 200));
		setLayout(new MigLayout("insets 4pt, gap 8pt, fillx", "10[][]10"));
		label = new JLabel(title);
		add(label, "push");
		checkbox = new JCheckBox();
		GuiUtils.styleCheckboxUsingEye(checkbox);
		checkbox.setText("");
		checkbox.setOpaque(false);
		add(checkbox);
	}

	public JCheckBox getCheckBox() {
		return checkbox;
	}

	public void setTitle(String title) {
		label.setText(title);
	}

	public String getTitle() {
		return label.getText();
	}

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setLayout(new MigLayout("", "[grow]", "[grow]"));
		frame.add(new GroupTitle("Group"), "grow");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}
}
