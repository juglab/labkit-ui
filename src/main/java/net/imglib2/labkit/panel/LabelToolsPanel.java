package net.imglib2.labkit.panel;

import bdv.util.BdvHandle;
import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.control.brush.LabelBrushController;
import net.imglib2.labkit.control.brush.MoveBrush;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.InputTriggerMap;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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
	private ViewerPanel viewerPanel;
	private MoveBrush moveBehaviour;
	private MouseAdapter mouseOverlay;

	ButtonGroup mainGroup = new ButtonGroup();

	public LabelToolsPanel(BdvHandle bdvHandle, ActionMap actions, LabelBrushController brushController) {
		this.brushController = brushController;
		this.actions = actions;
		this.viewerPanel = bdvHandle.getViewerPanel();
		triggerBindings = bdvHandle.getTriggerbindings();
		moveBehaviour = (MoveBrush) brushController.getBehaviour("move brush");
		mouseOverlay = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				moveBehaviour.init(e.getX(), e.getY());
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				moveBehaviour.drag(e.getX(), e.getY());
			}
			@Override
			public void mouseExited(MouseEvent e) {
				moveBehaviour.end(e.getX(), e.getY());
			}
		};

		setLayout(new MigLayout("flowy, insets 4pt, gap 4pt", "[][][][][]", "[][]push"));
		setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
		JToggleButton moveBtn = createActionButton("Move", null, "/images/move.png", new MoveBtnListener());
		createActionButton("Draw (D)", actions.get("paint"), "/images/draw.png", new PaintBtnListener());
		createActionButton("Erase", actions.get(drawEraseMode ? "erase" : "floodclear"), "/images/erase.png", new EraseBtnListener());
		createActionButton("Flood Fill (F)", actions.get("floodfill"), "/images/fill.png", new FillBtnListener());
		optionPane = new JPanel();
		GridLayout gridLayout = new GridLayout(0, 2);
		optionPane.setLayout(gridLayout);
		addEraseOptions(optionPane);
		addBrushSizeOption(optionPane);
		add(optionPane, "wrap, pushy");
		JButton help = new JButton();
		help.setIcon(new ImageIcon(this.getClass().getResource("/images/help.png")));
		help.setMargin(new Insets(0,0,0,0));
		help.addActionListener(new HelpWindow());
		add(help, "spany, push, al right");

		btn1.doClick();
		moveBtn.doClick();
	}

	private JToggleButton createActionButton(String buttonTitle, Action action, String iconPath, ItemListener listener) {
		JToggleButton button = new JToggleButton(action);
		button.setIcon(new ImageIcon(this.getClass().getResource(iconPath)));
		button.setToolTipText(buttonTitle);
		button.setMargin(new Insets(0,0,0,0));
		button.addItemListener(listener);
		mainGroup.add(button);
		add(button, "wrap, spany");
		return button;
	}

	private void addEraseOptions(JPanel panel) {
		btn1 = createRadioButton("Erase area on mouse click", "Floodremove (R) to remove one connected component of a label");
		JRadioButton btn2 = createRadioButton("Erase stroke on mouse drag", "Erase (E) where you drag the mouse");
		ButtonGroup group = new ButtonGroup();
		group.add(btn1);
		group.add(btn2);
		btn1.addItemListener(new DrawEraseModeBtnListener());
		eraseOptions = new JPanel();
		eraseOptions.setLayout(new GridLayout(2,0));
		eraseOptions.add(btn1);
		eraseOptions.add(btn2);
		eraseOptions.setVisible(false);
		panel.add(eraseOptions);
	}

	private void addBrushSizeOption(JPanel panel) {
		JSlider brushSize = new JSlider();
		brushSizeOptions = new JPanel();
		brushSizeOptions.setLayout(new GridLayout(2,0));
		brushSizeOptions.add(new Label("Brush size:"));
		brushSizeOptions.add(brushSize);
		panel.add(brushSizeOptions);
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

	private void registerMouseMotion() {
		viewerPanel.getDisplay().addMouseListener(mouseOverlay);
		viewerPanel.getDisplay().addMouseMotionListener(mouseOverlay);
	}

	private void unregisterMouseMotion() {
		viewerPanel.getDisplay().removeMouseListener(mouseOverlay);
		viewerPanel.getDisplay().removeMouseMotionListener(mouseOverlay);
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
			unregisterMouseMotion();
			triggerBindings.removeBehaviourMap("blockTranslation");
		}
		@Override
		protected void disableAction() {
			registerMouseMotion();
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
			setBrushSizeOptionVisible(true);
			invalidate();
		}
		@Override
		protected void disableAction() {
			setBrushSizeOptionVisible(false);
		}
	}

	private class EraseBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			addBindings(drawEraseMode ? "erase" : "floodclear", "button1");
			setEraseOptionsVisible(true);
			setBrushSizeOptionVisible(drawEraseMode);
		}
		@Override
		protected void disableAction() {
			setEraseOptionsVisible(false);
			setBrushSizeOptionVisible(false);
		}
	}

	private class FillBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			addBindings("floodfill", "button1");
		}
	}

	private class DrawEraseModeBtnListener extends LabkitBtnListener {
		@Override
		protected void enableAction() {
			drawEraseMode = false;
			setBrushSizeOptionVisible(false);
			addBindings("floodclear", "button1");
		}
		@Override
		protected void disableAction() {
			drawEraseMode = true;
			setBrushSizeOptionVisible(true);
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
