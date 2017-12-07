package net.imglib2.atlas.panel;

import net.imglib2.atlas.Extensible;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class LabelPanel {

	private final Extensible extensible;

	private final JPanel panel = new JPanel();

	private final JList view = new JList();

	public LabelPanel(Extensible extensible) {
		this.extensible = extensible;
		panel.setLayout(new MigLayout("","[grow]", "[grow]"));
		panel.add(new JScrollPane(view), "grow");
	}



	public JComponent getComponent() {
		return panel;
	}
}
