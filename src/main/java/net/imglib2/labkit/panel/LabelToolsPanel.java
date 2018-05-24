package net.imglib2.labkit.panel;

import bdv.util.BdvHandle;
import net.imglib2.labkit.control.brush.LabelBrushController;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class LabelToolsPanel extends JPanel {

	private boolean drawEraseMode = true;
	private final TriggerBehaviourBindings triggerBindings;
	LabelBrushController brushController;
	private final ActionMap actions;
	private final static String btnBehaviourId = "panel";
	private JPanel eraseOptions;
	private JPanel brushSizeOptions;
	private JPanel optionPane;
	private JRadioButton btn1;

	public LabelToolsPanel(BdvHandle bdvHandle, ActionMap actions, LabelBrushController brushController) {
		this.brushController = brushController;
		this.actions = actions;
		triggerBindings = bdvHandle.getTriggerbindings();
		ButtonGroup group = new ButtonGroup();
		setLayout(new MigLayout("flowy, insets 4pt, gap 4pt, top", "[][][][][]", "[][]push"));
		setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
		JToggleButton moveBtn = createActionButton("Move", null, "/images/move.png");
		JToggleButton paintBtn = createActionButton("Draw (D)", actions.get("paint"), "/images/draw.png");
		JToggleButton eraseBtn = createActionButton("Erase", actions.get(drawEraseMode ? "erase" : "floodclear"), "/images/erase.png");
		JToggleButton fillBtn = createActionButton("Flood Fill (F)\nThis only works properly on 2D images", actions.get("floodfill"), "/images/fill.png");
		moveBtn.addItemListener(new MoveBtnListener());
		paintBtn.addItemListener(new PaintBtnListener());
		eraseBtn.addItemListener(new EraseBtnListener());
		fillBtn.addItemListener(new FillBtnListener());
		group.add(moveBtn);
		group.add(paintBtn);
		group.add(eraseBtn);
		group.add(fillBtn);
		add(moveBtn, "wrap");
		add(paintBtn, "wrap");
		add(fillBtn, "wrap");
		add(eraseBtn, "wrap");
		optionPane = new JPanel();
		optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.LINE_AXIS));
		addEraseOptions(optionPane);
		addBrushSizeOption(optionPane);
		add(optionPane, "wrap, growy, hmin 50px");
		JButton help = new JButton();
		help.setIcon(new ImageIcon(this.getClass().getResource("/images/help.png")));
		help.setMargin(new Insets(0,0,0,0));
		help.addActionListener(new HelpWindow());
		add(help, "push, al right");
//		setMinimumSize(new Dimension(0, optionPane.getHeight()));
//		btn1.doClick();
//		moveBtn.doClick();
	}

	private JToggleButton createActionButton(String buttonTitle, Action action, String iconPath) {
		JToggleButton button = new JToggleButton(action);
		button.setIcon(new ImageIcon(this.getClass().getResource(iconPath)));
		button.setToolTipText(buttonTitle);
		button.setMargin(new Insets(0,0,0,0));
		return button;
	}

	private void addEraseOptions(JPanel panel) {
		btn1 = createRadioButton("Erase area on mouse click", "Floodremove (R) to remove one connected component of a label");
		JRadioButton btn2 = createRadioButton("Erase stroke on mouse drag", "Erase (E) where you drag the mouse");
		ButtonGroup group = new ButtonGroup();
		group.add(btn1);
		group.add(btn2);
		btn1.setOpaque(false);
		btn2.setOpaque(false);
		btn1.addItemListener(new DrawEraseModeBtnListener());
		eraseOptions = new JPanel();
		eraseOptions.setLayout(new MigLayout("insets 2pt, gap 0, top"));
		eraseOptions.add(btn1, "wrap");
		eraseOptions.add(btn2);
		eraseOptions.setBackground(new Color(250,250,250));
		eraseOptions.setBorder(BorderFactory.createLineBorder(new Color(170,170,170)));
		panel.add(eraseOptions, "al left, growy");
	}

	private void addBrushSizeOption(JPanel panel) {
		JSlider brushSize = new JSlider();
		brushSizeOptions = new JPanel();
		brushSizeOptions.setLayout(new MigLayout("insets 2pt, gap 0"));
		JLabel label = new JLabel("Brush size:");
		label.setOpaque(false);
		brushSize.setOpaque(false);
		brushSizeOptions.add(label, "wrap");
		brushSizeOptions.add(brushSize);
		brushSizeOptions.setBackground(new Color(250,250,250));
		brushSizeOptions.setBorder(BorderFactory.createLineBorder(new Color(170,170,170)));
		panel.add(brushSizeOptions, "al left, growy");
	}

	private JRadioButton createRadioButton(String name, String toolTip) {
		JRadioButton button = new JRadioButton(name);
		button.setToolTipText(toolTip);
		button.setMargin(new Insets(0,0,0,0));
		return button;
	}

	private void setBrushSizeOptionVisible(boolean visible) {
		brushSizeOptions.setVisible(visible);
	}

	private void setEraseOptionsVisible(boolean visible) {
		eraseOptions.setVisible(visible);
	}

	private void updateOptionsPane(boolean brushSizeVisible, boolean eraseVisible) {
		setBrushSizeOptionVisible(brushSizeVisible);
		setEraseOptionsVisible(eraseVisible);
//		invalidate();
	}

	private abstract class LabkitBtnListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent ev) {
			if(ev.getStateChange()==ItemEvent.SELECTED){
				enableAction();
			} else if(ev.getStateChange()== ItemEvent.DESELECTED){
				disableAction();
			}
		}

		protected abstract void enableAction();

		protected void disableAction() {
			removeBindings();
		}

		protected void addBindings(String behaviour, String trigger) {
			final BehaviourMap paint = new BehaviourMap();
			paint.put(btnBehaviourId, brushController.getBehaviour(behaviour));
			final InputTriggerMap paintTrigger = new InputTriggerMap();
			paintTrigger.put(InputTrigger.getFromString(trigger), behaviour);
			triggerBindings.addInputTriggerMap(btnBehaviourId, paintTrigger);
			triggerBindings.addBehaviourMap(btnBehaviourId, paint);
		}

		protected void removeBindings() {
			triggerBindings.removeInputTriggerMap(btnBehaviourId);
			triggerBindings.removeBehaviourMap(btnBehaviourId);
		}

	}

	private class MoveBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			triggerBindings.removeBehaviourMap("blockTranslation");
			updateOptionsPane(false, false);
		}
		@Override
		protected void disableAction() {
			final BehaviourMap blockTranslation = new BehaviourMap();
			blockTranslation.put("drag rotate", new Behaviour() {});
			blockTranslation.put("2d drag rotate", new Behaviour() {});
			triggerBindings.addBehaviourMap("blockTranslation", blockTranslation);
		}
	}

	private class PaintBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			addBindings("paint", "button1");
			updateOptionsPane(true, false);
		}
		@Override
		protected void disableAction() {
			updateOptionsPane(false, false);
		}
	}

	private class EraseBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			addBindings(drawEraseMode ? "erase" : "floodclear", "button1");
			updateOptionsPane(drawEraseMode, true);
		}
		@Override
		protected void disableAction() {
			updateOptionsPane(false, false);
		}
	}

	private class FillBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			addBindings("floodfill", "button1");
			updateOptionsPane(false, false);
		}
	}

	private class DrawEraseModeBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			drawEraseMode = false;
			updateOptionsPane(false, true);
			addBindings("floodclear", "button1");
		}
		@Override
		protected void disableAction() {
			drawEraseMode = true;
			updateOptionsPane(true, true);
			addBindings("erase", "button1");
		}
	}

	private class HelpWindow implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String text = "<html><div style='text-align:right;'>To switch between labels:<br />Press N on the keyboard or<br />select the label on the left panel.</div>";
			JOptionPane.showMessageDialog(null, text,
					"Help", JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
