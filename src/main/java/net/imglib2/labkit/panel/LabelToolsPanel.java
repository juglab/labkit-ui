package net.imglib2.labkit.panel;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LabelToolsPanel extends JPanel {

	boolean drawEraseMode = true;

	public LabelToolsPanel(ActionMap actions) {
		ButtonGroup group = new ButtonGroup();
		setLayout(new MigLayout("flowy, insets 4pt, gap 4pt", "[][][][]push[]", "[][]push"));
		setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
		JToggleButton paintBtn = createActionButton("Draw (D)", actions.get("paint"), "/images/draw.png");
		JToggleButton eraseBtn = createActionButton("Erase", actions.get(drawEraseMode ? "erase" : "floodclear"), "/images/erase.png");
		JToggleButton fillBtn = createActionButton("Flood Fill (F)\nThis only works properly on 2D images", actions.get("floodfill"), "/images/fill.png");
		group.add(paintBtn);
		group.add(eraseBtn);
		group.add(fillBtn);
		add(paintBtn, "wrap, spany, grow");
		add(fillBtn, "wrap, spany, grow");
		add(eraseBtn, "wrap, spany, grow");
		addEraseOptions(actions);
		add(createSwitchLabelHint(), "spany, push, al right");
	}

	private Component createSwitchLabelHint() {
		JLabel label = new JLabel("<html><div style='text-align:right;'>To switch between labels:<br />Press N on the keyboard or<br />select the label on the left panel.</div>");
		return label;
	}

	private JToggleButton createActionButton(String buttonTitle, Action action, String iconPath) {
		JToggleButton button = new JToggleButton(action);
		button.setIcon(new ImageIcon(this.getClass().getResource(iconPath)));
		button.setToolTipText(buttonTitle);
		button.setMargin(new Insets(0,0,0,0));
		return button;
	}

	private void addEraseOptions(ActionMap actions) {
		JRadioButton btn1 = createRadioButton("Erase area on mouse click", "Floodremove (R) to remove one connected component of a label");
		JRadioButton btn2 = createRadioButton("Erase stroke on mouse drag", "Erase (E) where you drag the mouse");
		ButtonGroup group = new ButtonGroup();
		group.add(btn1);
		group.add(btn2);
		add(btn1, "gapafter 0");
		add(btn2, "wrap, gapbefore 0");
	}

	private JRadioButton createRadioButton(String name, String toolTip) {
		JRadioButton button = new JRadioButton(name);
		button.setToolTipText(toolTip);
		button.setMargin(new Insets(0,0,0,0));
		return button;
	}
}
