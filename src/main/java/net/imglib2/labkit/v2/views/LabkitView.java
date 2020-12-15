
package net.imglib2.labkit.v2.views;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LabkitView extends JFrame {

	private final JButton addImageButton = new JButton("add");

	private final DefaultListModel<String> imageList = new DefaultListModel<>();

	public LabkitView() {
		add(new JLabel("still empty"));
		add(rightPanel(), BorderLayout.LINE_END);
		setSize(800, 600);
	}

	private JPanel rightPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[grow][]"));
		panel.add(new JScrollPane(new JList<>(imageList)), "grow, wrap");
		panel.add(addImageButton);
		return panel;
	}

	// Getter

	public JButton getAddImageButton() {
		return addImageButton;
	}

	public static void main(String... args) {
		new LabkitView().setVisible(true);
	}

	public DefaultListModel<String> getImageList() {
		return imageList;
	}
}
