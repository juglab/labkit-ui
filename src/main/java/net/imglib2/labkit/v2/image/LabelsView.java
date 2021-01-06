
package net.imglib2.labkit.v2.image;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.panel.ComponentList;
import net.imglib2.labkit.panel.GuiUtils;
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
import java.util.List;

/**
 * Panel that shows the list of labels.
 */
public class LabelsView extends JPanel {

	private final LabelsModel model;
	private final ComponentList<Label, JPanel> list = new ComponentList<>();
	private final JFrame dialogParent;
	private final LabelsViewListener listener;

	public LabelsView(JFrame dialogParent, LabelsModel model,
		LabelsViewListener listener)
	{
		this.dialogParent = dialogParent;
		this.model = model;
		this.listener = listener;
		setLayout(new BorderLayout());
		add(initPanel(false));
		updateList();
	}

//	public static JPanel newFramedLabelPanel(
//		LabelingModel imageLabelingModel, DefaultExtensible extensible,
//		boolean fixedLabels)
//	{
//		return GuiUtils.createCheckboxGroupedPanel(imageLabelingModel
//			.labelingVisibility(), "Labeling", new LabelsView(extensible
//				.dialogParent(), new ColoredLabelsModel(imageLabelingModel),
//				fixedLabels, item1 -> extensible.createPopupMenu(Label.LABEL_MENU,
//					item1)).getComponent());
//	}

	// -- Helper methods --

	public void updateList() {
		list.clear();
		List<Label> items = model.getLabels();
		items.forEach((label) -> list.add(label, new EntryPanel(label)));
	}

	public void updateSelectedLabel() {
		list.setSelected(model.getActiveLabel());
	}

	private JPanel initPanel(boolean fixedLabels) {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		list.listeners().addListener(this::onSelectedLabelChanged);
		list.getComponent().setBorder(BorderFactory.createEmptyBorder());
		panel.add(list.getComponent(), "grow, span, push, wrap");
		if (!fixedLabels) {
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBackground(UIManager.getColor("List.background"));
			buttonsPanel.setLayout(new MigLayout("insets 4pt, gap 4pt", "[grow]",
				""));
			buttonsPanel.add(GuiUtils.createActionIconButton("Add label",
				new RunnableAction("Add label", listener::addLabel), "add.png"), "");
			buttonsPanel.add(GuiUtils.createActionIconButton("Remove all",
				new RunnableAction("Remove all", listener::removeAllLabels), "remove.png"),
				"gapbefore push");
			panel.add(buttonsPanel, "grow, span");
		}
		return panel;
	}

	private void onSelectedLabelChanged() {
		Label label = list.getSelected();
		if (label != null)
			listener.setActiveLabel(label);
	}

	private void onRenameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(dialogParent,
			"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		listener.renameLabel(label, newName);
	}

	private void changeColor(Label label) {
		ARGBType color = label.color();
		Color newColor = JColorChooser.showDialog(dialogParent,
			"Choose Color for Label \"" + label.name() + "\"", new Color(color
				.get()));
		if (newColor == null) return;
		listener.setColor(label, new ARGBType(newColor.getRGB()));
	}

	private void onFocusLabel(Label label) {
		listener.focusLabel(label);
	}

	// -- Helper methods --
	private class EntryPanel extends JPanel {

		private final Label label;

		EntryPanel(Label label) {
			this.label = label;
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			add(initColorButton());
			add(new JLabel(label.name()), "grow, push, width 0:0:pref");
			JPopupMenu menu = listener.getPopupMenu(label);
			add(initPopupMenuButton(menu));
			setComponentPopupMenu(menu);
			add(initFinderButton(), "gapx 4pt");
			add(initVisibilityCheckbox());
			initRenameOnDoubleClick();
		}

		private JCheckBox initVisibilityCheckbox() {
			JCheckBox checkBox = GuiUtils.styleCheckboxUsingEye(new JCheckBox());
			checkBox.setSelected(label.isVisible());
			checkBox.addItemListener(event -> {
				listener.setLabelVisibility(label, event.getStateChange() == ItemEvent.SELECTED);
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

		private void initRenameOnDoubleClick() {
			addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2) onRenameLabel(label);
				}
			});
		}

		private JButton initColorButton() {
			JButton colorButton = new JButton();
			colorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
			colorButton.setIcon(GuiUtils.createIcon(new Color(label.color().get())));
			colorButton.addActionListener(l -> changeColor(label));
			return colorButton;
		}

		private JButton initFinderButton() {
			return GuiUtils.createIconButton(GuiUtils.createAction("locate",
				() -> onFocusLabel(label), "crosshair.png"));
		}

	}

}
