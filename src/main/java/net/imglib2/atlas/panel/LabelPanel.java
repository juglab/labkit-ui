package net.imglib2.atlas.panel;

import net.imglib2.atlas.Extensible;
import net.imglib2.atlas.Holder;
import net.imglib2.atlas.color.ColorMapProvider;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class LabelPanel {

	private final DefaultListModel<String> model = new DefaultListModel<>();
	private final ColorMapProvider colorMapProvider;
	private JList<String> list = new JList<>(model);
	private final JPanel panel = initPanel();
	private final Extensible extensible;
	private Holder<Labeling> labeling;

	public LabelPanel(Extensible extensible, ColorMapProvider colorMapProvider) {
		this.colorMapProvider = colorMapProvider;
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
		panel.setPreferredSize(new Dimension(200, 100));
		panel.setLayout(new MigLayout("","[grow]", "[grow][][][]"));
		list.setCellRenderer(new MyRenderer());
		panel.add(new JScrollPane(list), "grow, wrap");
		panel.add(new JButton(new RunnableAction("add", this::addLabel)), "grow, wrap");
		panel.add(new JButton(new RunnableAction("remove", () -> doForSelectedLabel(this::removeLabel))), "grow, wrap");
		panel.add(new JButton(new RunnableAction("rename", () -> doForSelectedLabel(this::renameLabel))), "grow");
		return panel;
	}

	private void doForSelectedLabel(Consumer<String> action) {
		int index = list.getSelectedIndex();
		if(index < 0)
			return;
		String label = list.getModel().getElementAt(index);
		action.accept(label);
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

	private String suggestName(List<String> labels) {
		for (int i = 1; i < 10000; i++) {
			String label = "Label " + i;
			if (!labels.contains(label))
				return label;
		}
		return null;
	}

	// -- Helper class --

	private class MyRenderer implements ListCellRenderer<String> {

		MyRenderer() {
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
			ARGBType color = colorMapProvider.colorMap().getColor(value);
			JPanel panel = new JPanel();
			panel.setOpaque(true);
			panel.setLayout(new MigLayout());
			JButton comp = new JButton();
			comp.setBackground(new Color(color.get()));
			panel.add(comp);
			panel.add(new JLabel(value));
			panel.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
			panel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
			return panel;
		}
	}

}
