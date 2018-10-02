
package net.imglib2.labkit.panel;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LabelPanel {

	private final ColoredLabelsModel model;
	private ComponentList<Label, JPanel> list = new ComponentList<>();
	private final JPanel panel;
	private final JFrame dialogParent;
	private final boolean fixedLabels;

	public LabelPanel(JFrame dialogParent, ColoredLabelsModel model,
		boolean fixedLabels)
	{
		this.model = model;
		this.dialogParent = dialogParent;
		this.panel = initPanel(fixedLabels);
		this.fixedLabels = fixedLabels;
		model.listeners().add(this::update);
		update();
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void update() {
		list.clear();
		List<Label> items = model.items();
		items.forEach((label) -> list.add(label, new EntryPanel(label)));
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
				new RunnableAction("Add label", this::addLabel), "add.png"), "");
			buttonsPanel.add(GuiUtils.createActionIconButton("Remove all",
				new RunnableAction("Remove all", this::removeAllLabels), "remove.png"),
				"gapbefore push");
			panel.add(buttonsPanel, "grow, span");
		}
		return panel;
	}

	private void changeSelectedLabel() {
		Label label = list.getSelected();
		if (label != null) model.setSelected(label);
	}

	private void addLabel() {
		model.addLabel();
	}

	private void removeLabel(Label label) {
		model.removeLabel(label);
	}

	private void removeAllLabels() {
		List<Label> items = new ArrayList<>(model.items());
		items.forEach(model::removeLabel);
	}

	private void clearLabel(Label label) {
		model.clearLabel(label);
	}

	private void renameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(dialogParent,
			"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		model.renameLabel(label, newName);
	}

	private void moveUpLabel(Label label) {
		model.moveLabel(label, -1);
	}

	private void moveDownLabel(Label label) {
		model.moveLabel(label, 1);
	}

	private void changeColor(Label label) {
		ARGBType color = label.color();
		Color newColor = JColorChooser.showDialog(dialogParent,
			"Choose Color for Label \"" + label.name() + "\"", new Color(color
				.get()));
		if (newColor == null) return;
		model.setColor(label, new ARGBType(newColor.getRGB()));
	}

	private void localize(Label label) {
		model.localizeLabel(label);
	}

	// -- Helper methods --
	private class EntryPanel extends JPanel {

		EntryPanel(Label value) {
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			add(initColorButton(value));
			add(new JLabel(value.name()), "push");
			JPopupMenu menu = initPopupMenu(value);
			add(initPopupMenuButton(menu));
			setComponentPopupMenu(menu);
			add(initFinderButton(value), "gapx 4pt");
			add(initVisibilityCheckbox(value));
			initRenameOnDoubleClick(value);
		}

		private JCheckBox initVisibilityCheckbox(Label label) {
			JCheckBox checkBox = GuiUtils.styleCheckboxUsingEye(new JCheckBox());
			checkBox.setSelected(label.isActive());
			checkBox.addItemListener(event -> {
				model.setActive(label, event.getStateChange() == ItemEvent.SELECTED);
			});
			checkBox.setOpaque(false);
			return checkBox;
		}

		private JButton initPopupMenuButton(JPopupMenu menu) {
			JButton button = new BasicArrowButton(BasicArrowButton.SOUTH);
			button.addActionListener(actionEvent -> {
				menu.show(button, 0, button.getHeight());
			});
			return button;
		}

		private void initRenameOnDoubleClick(Label value) {
			addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2) renameLabel(value);
				}
			});
		}

		private JPopupMenu initPopupMenu(Label label) {
			JPopupMenu menu = new JPopupMenu();
			if (!fixedLabels) menu.add(new JMenuItem(new RunnableAction("Rename",
				() -> renameLabel(label))));
			if (!fixedLabels) menu.add(new JMenuItem(new RunnableAction("Move up",
				() -> moveUpLabel(label))));
			if (!fixedLabels) menu.add(new JMenuItem(new RunnableAction("Move down",
				() -> moveDownLabel(label))));
			menu.add(new JMenuItem(new RunnableAction("Clear", () -> clearLabel(
				label))));
			if (!fixedLabels) menu.add(new JMenuItem(new RunnableAction("Remove",
				() -> removeLabel(label))));
			return menu;
		}

		private JButton initColorButton(Label value) {
			JButton colorButton = new JButton();
			colorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
			colorButton.setIcon(GuiUtils.createIcon(new Color(value.color().get())));
			colorButton.addActionListener(l -> changeColor(value));
			return colorButton;
		}

		private JButton initFinderButton(Label label) {
			return GuiUtils.createIconButton(GuiUtils.createAction("locate",
				() -> localize(label), "crosshair.png"));
		}

	}

}
