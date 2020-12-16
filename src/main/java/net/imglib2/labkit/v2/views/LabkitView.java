
package net.imglib2.labkit.v2.views;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LabkitView extends JFrame {

	private final JButton addImageButton = new JButton("add");

	private final JList<String> imageList = new JList<>();

	private final JLabel activeImageLabel = new JLabel("-");

	public LabkitView() {
		add(activeImageLabel);
		add(rightPanel(), BorderLayout.LINE_END);
		setSize(800, 600);
	}

	private JPanel rightPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		panel.add(new JLabel("Images"), "wrap");
		panel.add(new JScrollPane(imageList), "grow, wrap");
		panel.add(addImageButton);
		return panel;
	}

	// Getter

	public JButton getAddImageButton() {
		return addImageButton;
	}

	public JList<String> getImageList() {
		return imageList;
	}

	public JLabel getActiveImageLabel() {
		return activeImageLabel;
	}

	// demo

	public static void main(String... args) {
		new LabkitView().setVisible(true);
	}
}
