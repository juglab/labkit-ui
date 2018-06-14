
package net.imglib2.labkit.panel;

import java.awt.Color;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import net.imglib2.labkit.models.ColoredLabel;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;

import org.scijava.ui.behaviour.util.RunnableAction;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabelPanel {

	private final ColoredLabelsModel model;
	private ComponentList<String, JPanel> list = new ComponentList<>();
	private final JPanel panel;
	private final JFrame dialogParent;

	public LabelPanel(JFrame dialogParent, ColoredLabelsModel model,
		boolean fixedLabels)
	{
		this.model = model;
		this.dialogParent = dialogParent;
		this.panel = initPanel(fixedLabels);
		model.listeners().add(this::update);
		update();
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void update() {
		list.clear();
		List<ColoredLabel> items = model.items();
		items.forEach((label) -> list.add(label.name, new EntryPanel(label.name,
			label.color)));
		list.setSelected(model.selected());
	}

	private JPanel initPanel(boolean fixedLabels) {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		list.listeners().add(this::changeSelectedLabel);
		list.getComponent().setBorder(BorderFactory.createEmptyBorder());
		panel.add(list.getComponent(), "grow, span, push, wrap");
		if (!fixedLabels) {
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBackground(UIManager.getColor("List.background"));
			buttonsPanel.setLayout(new MigLayout("insets 4pt, gap 4pt", "[grow]",
				""));
			buttonsPanel.add(GuiUtils.createActionIconButton("Add label",
				new RunnableAction("Add label", this::addLabel), "/images/add.png"),
				"");
			buttonsPanel.add(GuiUtils.createActionIconButton("Remove all",
				new RunnableAction("Remove all", this::removeAllLabels),
				"/images/remove.png"), "gapbefore push");
			panel.add(buttonsPanel, "grow, span");
		}
		return panel;
	}

	private void changeSelectedLabel() {
		String label = list.getSelected();
		if (label != null) model.setSelected(label);
	}

	private void addLabel() {
		model.addLabel();
	}

	private void removeLabel(String label) {
		model.removeLabel(label);
	}

	private void removeAllLabels() {
		List<ColoredLabel> items = model.items();
		items.forEach((label) -> model.removeLabel(label.name));
	}

	private void clearLabel(String label) {
		model.clearLabel(label);
	}

	private void renameLabel(String label) {
		String newLabel = JOptionPane.showInputDialog(dialogParent,
			"Rename label \"" + label + "\" to:", label);
		if (newLabel == null) return;
		model.renameLabel(label, newLabel);
	}

	private void moveUpLabel(String label) {
		model.moveLabel(label, -1);
	}

	private void moveDownLabel(String label) {
		model.moveLabel(label, 1);
	}

	private void changeColor(String label) {
		ARGBType color = model.getColor(label);
		Color newColor = JColorChooser.showDialog(dialogParent,
			"Choose Color for Label \"" + label + "\"", new Color(color.get()));
		if (newColor == null) return;
		model.setColor(label, new ARGBType(newColor.getRGB()));
	}

	private void localize(String label) {
		model.localizeLabel(label);
	}

	// -- Helper methods --
	private class EntryPanel extends JPanel {

		EntryPanel(String value, ARGBType color) {
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			add(initColorButton(value, color));
			add(new JLabel(value), "push");
			JPopupMenu menu = initPopupMenu(value);
			add(initPopupMenuButton(menu));
			setComponentPopupMenu(menu);
			add(initFinderButton(value));
			initRenameOnDoubleClick(value);
		}

		private JButton initPopupMenuButton(JPopupMenu menu) {
			JButton button = new JButton("...");
			button.setBorder(BorderFactory.createEmptyBorder());
			button.setContentAreaFilled(false);
			button.setOpaque(false);
			button.addActionListener(actionEvent -> {
				menu.show(button, 0, button.getHeight());
			});
			return button;
		}

		private void initRenameOnDoubleClick(String value) {
			addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2) renameLabel(value);
				}
			});
		}

		private JPopupMenu initPopupMenu(String value) {
			JPopupMenu menu = new JPopupMenu();
			menu.add(new JMenuItem(new RunnableAction("Rename", () -> renameLabel(
				value))));
			menu.add(new JMenuItem(new RunnableAction("Move up", () -> moveUpLabel(
				value))));
			menu.add(new JMenuItem(new RunnableAction("Move down",
				() -> moveDownLabel(value))));
			menu.add(new JMenuItem(new RunnableAction("Clear", () -> clearLabel(
				value))));
			menu.add(new JMenuItem(new RunnableAction("Remove", () -> removeLabel(
				value))));
			return menu;
		}

		private JButton initColorButton(String value, ARGBType color) {
			JButton colorButton = new JButton();
			colorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
			colorButton.setIcon(GuiUtils.createIcon(new Color(color.get())));
			colorButton.addActionListener(l -> changeColor(value));
			return colorButton;
		}

		private JButton initFinderButton(String value) {
			JButton finder = new JButton();
			finder.setBorder(BorderFactory.createEmptyBorder());
			finder.setContentAreaFilled(false);
			finder.setOpaque(false);
			finder.setIcon(new ImageIcon(getClass().getResource(
				"/images/crosshair.png")));
			finder.addActionListener(l -> localize(value));
			return finder;
		}
	}

}
