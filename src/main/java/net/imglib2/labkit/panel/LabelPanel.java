package net.imglib2.labkit.panel;

import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.Holder;
import net.imglib2.labkit.ImageLabelingModel;
import net.imglib2.labkit.color.ColorMap;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class LabelPanel {

	private final ImageLabelingModel model;
	private ComponentList<String, JPanel> list = new ComponentList<>();
	private final JPanel panel = initPanel();
	private final Extensible extensible;
	private Holder<Labeling> labeling;

	public LabelPanel(Extensible extensible, ImageLabelingModel model) {
		this.model = model;
		this.extensible = extensible;
		this.labeling = extensible.labeling();
		labeling.notifier().add(this::updateLabeling);
		updateLabeling(labeling.get());
		model.selectedLabel().notifier().add(this::viewSelectedLabel);
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void updateLabeling(Labeling labeling) {
		list.clear();
		labeling.getLabels().forEach(label -> list.add(label, new EntryPanel(label)));
	}

	private JPanel initPanel() {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(200, 100));
		panel.setLayout(new MigLayout("insets 0","[grow]", "[][grow][][][]"));
		panel.add(new JLabel("Labels:"), "wrap");
		list.listeners().add(this::changeSelectedLabel);
		panel.add(list.getCompnent(), "grow, wrap");
		panel.add(new JButton(new RunnableAction("add", this::addLabel)), "grow, split 2");
		panel.add(new JButton(new RunnableAction("remove", () -> doForSelectedLabel(this::removeLabel))), "grow, wrap");
		panel.add(new JButton(new RunnableAction("rename", () -> doForSelectedLabel(this::renameLabel))), "grow, wrap");
		return panel;
	}

	private void viewSelectedLabel(String label) {
		list.setSelected(label);
	}

	private void changeSelectedLabel() {
		String label = getSelectedLabel();
		if(label != null)
			model.selectedLabel().set(label);
	}

	private void doForSelectedLabel(Consumer<String> action) {
		String label = getSelectedLabel();
		if(label != null) action.accept(label);
	}

	private String getSelectedLabel() {
		return list.getSelected();
	}

	private void addLabel() {
		String label = suggestName(labeling.get().getLabels());
		if(label == null)
			return;
		labeling.get().addLabel(label);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private void removeLabel(String label) {
		labeling.get().removeLabel(label);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private void renameLabel(String label) {
		String newLabel = JOptionPane.showInputDialog(extensible.dialogParent(), "Rename label \"" + label + "\" to:");
		if(newLabel == null)
			return;
		labeling.get().renameLabel(label, newLabel);
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private void changeColor(String label) {
		ColorMap colorMap = model.colorMapProvider().colorMap();
		ARGBType color = colorMap.getColor(label);
		Color newColor = JColorChooser.showDialog(extensible.dialogParent(), "Choose Color for Label \"" + label + "\"", new Color(color.get()));
		if(newColor == null) return;
		colorMap.setColor(label, new ARGBType(newColor.getRGB()));
		labeling.notifier().forEach(l -> l.accept(labeling.get()));
	}

	private String suggestName(List<String> labels) {
		for (int i = 1; i < 10000; i++) {
			String label = "Label " + i;
			if (!labels.contains(label))
				return label;
		}
		return null;
	}

	// -- Helper methods --

	private class EntryPanel extends JPanel {

		EntryPanel(String value) {
			ARGBType color = model.colorMapProvider().colorMap().getColor(value);
			setOpaque(true);
			setLayout(new MigLayout());
			JButton comp = new JButton();
			comp.setBackground(new Color(color.get()));
			comp.setOpaque(true); // Hope this makes the color visible on Mac
			comp.addActionListener(l -> changeColor(value));
			add(comp);
			add(new JLabel(value));
		}
	}
}
