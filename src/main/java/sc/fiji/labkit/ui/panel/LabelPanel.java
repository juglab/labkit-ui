/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.panel;

import sc.fiji.labkit.ui.DefaultExtensible;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.ColoredLabelsModel;
import sc.fiji.labkit.ui.models.LabelingModel;
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
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Panel that shows the list of labels.
 */
public class LabelPanel {

	private final ColoredLabelsModel model;
	private final ComponentList<Label, JPanel> list = new ComponentList<>();
	private final JPanel panel;
	private final JFrame dialogParent;
	private final Function<Supplier<Label>, JPopupMenu> menuFactory;

	public LabelPanel(JFrame dialogParent, ColoredLabelsModel model,
		boolean fixedLabels, Function<Supplier<Label>, JPopupMenu> menuFactory)
	{
		this.model = model;
		this.dialogParent = dialogParent;
		this.panel = initPanel(fixedLabels);
		this.menuFactory = menuFactory;
		model.listeners().addListener(this::update);
		model.selected().notifier().addListener(() -> list.setSelected(model.selected().get()));
		list.listeners().addListener(() -> model.selected().set(list.getSelected()));
		update();
	}

	public static JPanel newFramedLabelPanel(
		LabelingModel imageLabelingModel, DefaultExtensible extensible,
		boolean fixedLabels)
	{
		return GuiUtils.createCheckboxGroupedPanel(imageLabelingModel
			.labelingVisibility(), "Labeling", new LabelPanel(extensible
				.dialogParent(), new ColoredLabelsModel(imageLabelingModel),
				fixedLabels, item1 -> extensible.createPopupMenu(Label.LABEL_MENU,
					item1)).getComponent());
	}

	public JComponent getComponent() {
		return panel;
	}

	// -- Helper methods --

	private void update() {
		list.clear();
		List<Label> items = model.items();
		items.forEach((label) -> list.add(label, new EntryPanel(label)));
	}

	private JPanel initPanel(boolean fixedLabels) {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, gap 0", "[grow]", "[grow][]"));
		list.listeners().addListener(this::changeSelectedLabel);
		list.getComponent().setBorder(BorderFactory.createEmptyBorder());
		panel.add(list.getComponent(), "grow, span, push, wrap");
		if (!fixedLabels) {
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setBackground(UIManager.getColor("List.background"));
			buttonsPanel.setLayout(new MigLayout("insets 4pt, gap 4pt", "[grow]",
				""));
			buttonsPanel.add(initializeAddLabelButton(), "");
			buttonsPanel.add(GuiUtils.createActionIconButton("Remove all",
				new RunnableAction("Remove all", this::removeAllLabels), "remove.png"),
				"gapbefore push");
			panel.add(buttonsPanel, "grow, span");
		}
		return panel;
	}

	private JButton initializeAddLabelButton() {
		RunnableAction addLabelAction = new RunnableAction("Add label", this::addLabel);
		JButton button = GuiUtils.createActionIconButton("Add label", addLabelAction, "add.png");
		button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
			.put(KeyStroke.getKeyStroke("ctrl A"), "create new label");
		button.getActionMap().put("create new label", addLabelAction);
		button.setToolTipText("<html><small>Keyboard shortcut:</small></html>");
		return button;
	}

	private void changeSelectedLabel() {
		Label label = list.getSelected();
		if (label != null)
			model.selected().set(label);
	}

	private void addLabel() {
		model.addLabel();
	}

	private void removeAllLabels() {
		List<Label> items = new ArrayList<>(model.items());
		model.removeAllLabels(items);
	}

	private void renameLabel(Label label) {
		final String oldName = label.name();
		String newName = JOptionPane.showInputDialog(dialogParent,
			"Rename label \"" + oldName + "\" to:", oldName);
		if (newName == null) return;
		model.renameLabel(label, newName);
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

		private final Label label;

		EntryPanel(Label label) {
			this.label = label;
			setOpaque(true);
			setLayout(new MigLayout("insets 4pt, gap 4pt, fillx"));
			add(initColorButton());
			add(new JLabel(label.name()), "grow, push, width 0:0:pref");
			JPopupMenu menu = menuFactory.apply(() -> this.label);
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
				model.setActive(label, event.getStateChange() == ItemEvent.SELECTED);
			});
			checkBox.setOpaque(false);
			return checkBox;
		}

		private JButton initPopupMenuButton(JPopupMenu menu) {
			JButton button = new BasicArrowButton(BasicArrowButton.SOUTH);
			button.setFocusable(false);
			button.addActionListener(actionEvent -> {
				menu.show(button, 0, button.getHeight());
			});
			return button;
		}

		private void initRenameOnDoubleClick() {
			addMouseListener(new MouseAdapter() {

				public void mouseClicked(MouseEvent event) {
					if (event.getClickCount() == 2) renameLabel(label);
				}
			});
		}

		private JButton initColorButton() {
			JButton colorButton = new JButton();
			colorButton.setFocusable(false);
			colorButton.setBorder(new EmptyBorder(1, 1, 1, 1));
			colorButton.setIcon(GuiUtils.createIcon(new Color(label.color().get())));
			colorButton.addActionListener(l -> changeColor(label));
			return colorButton;
		}

		private JButton initFinderButton() {
			return GuiUtils.createIconButton(GuiUtils.createAction("locate",
				() -> localize(label), "crosshair.png"));
		}

	}

}
