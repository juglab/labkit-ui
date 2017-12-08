package net.imglib2.atlas.panel;

import net.imglib2.atlas.Extensible;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.labeling.Labeling;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;

public class LabelPanel {

	private final DefaultListModel<String> model = new DefaultListModel<>();
	private final JPanel panel = initPanel();
	private final Extensible extensible;
	private Holder<Labeling> labeling;

	public LabelPanel(Extensible extensible) {
		this.extensible = extensible;
		this.labeling = extensible.labeling();
		labeling.notifier().add(this::updateLabeling);
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void updateLabeling(Labeling labeling) {
		model.clear();
		labeling.getLabels().forEach(model::addElement);
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(100, 100));
		panel.setLayout(new MigLayout("","[grow]", "[grow][]"));
		JList<String> view = new JList<>(model);
		panel.add(new JScrollPane(view), "grow, wrap");
		panel.add(new JButton(new RunnableAction("add", this::addLabel)), "grow");
		return panel;
	}

	private void addLabel() {
		String label = JOptionPane.showInputDialog(extensible.dialogParent(), "Name for the new label");
		if(label == null)
			return;
		labeling.get().addLabel(label);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}
}
