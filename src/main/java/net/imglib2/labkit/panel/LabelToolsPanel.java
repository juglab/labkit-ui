
package net.imglib2.labkit.panel;

import bdv.util.BdvHandle;
import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.control.brush.LabelBrushController;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.*;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabelToolsPanel extends JPanel {

	private boolean drawEraseMode = true;
	private final TriggerBehaviourBindings triggerBindings;
	LabelBrushController brushController;
	private final static String btnBehaviourId = "panel";
	private JPanel eraseOptions;
	private JPanel brushSizeOptions;
	private JPanel optionPane;
	private JRadioButton btn1;
	private final Color optionsBorder = new Color(220, 220, 220);
	private final Color optionsBg = new Color(230, 230, 230);
	private final MouseAdapter brushMotionDrawer;
	private final ViewerPanel bdvPanel;
	private final ButtonGroup group = new ButtonGroup();

	public LabelToolsPanel(BdvHandle bdvHandle,
		LabelBrushController brushController)
	{
		this.brushController = brushController;
		triggerBindings = bdvHandle.getTriggerbindings();
		bdvPanel = bdvHandle.getViewerPanel();

		setLayout(new MigLayout("flowy, insets 0, gap 4pt, top", "[][][][][]",
			"[]push"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		JToggleButton moveBtn = addActionButton("Move", new MoveBtnListener(),
			"/images/move.png");
		addActionButton("Draw (D)", new PaintBtnListener(), "/images/draw.png");
		addActionButton("Erase", new EraseBtnListener(), "/images/erase.png");
		addActionButton("Flood Fill (F)\nThis only works properly on 2D images",
			new FillBtnListener(), "/images/fill.png");
		optionPane = new JPanel();
		optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.LINE_AXIS));
		addEraseOptions(optionPane);
		optionPane.add(Box.createRigidArea(new Dimension(5, 0)));
		addBrushSizeOption(optionPane);
		add(optionPane, "wrap, growy, hmin 50px");
		btn1.doClick();
		moveBtn.doClick();
		final DragBehaviour moveBrush = (DragBehaviour) brushController
			.getBehaviour("move brush");
		brushMotionDrawer = new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				moveBrush.init(e.getX(), e.getY());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				moveBrush.end(e.getX(), e.getY());
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				moveBrush.drag(e.getX(), e.getY());
			}
		};
	}

	private JToggleButton addActionButton(String buttonTitle,
		LabkitBtnListener btnListener, String iconPath)
	{
		JToggleButton button = new JToggleButton();
		button.setIcon(new ImageIcon(this.getClass().getResource(iconPath)));
		button.setToolTipText(buttonTitle);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addItemListener(btnListener);
		group.add(button);
		add(button, "wrap, top");
		return button;
	}

	private void addEraseOptions(JPanel panel) {
		btn1 = createRadioButton("Erase area on mouse click",
			"Floodremove (R) to remove one connected component of a label");
		JRadioButton btn2 = createRadioButton("Erase stroke on mouse drag",
			"Erase (E) where you drag the mouse");
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
		eraseOptions.setBackground(optionsBg);
		eraseOptions.setBorder(BorderFactory.createLineBorder(optionsBorder));
		panel.add(eraseOptions, "al left, growy");
	}

	private void addBrushSizeOption(JPanel panel) {
		JSlider brushSize = new JSlider(1, 50, (int) brushController
			.getBrushRadius());
		JLabel valLabel = new JLabel(String.valueOf(brushSize.getValue()));
		brushSize.setPaintTrack(true);
		brushSize.addChangeListener(e -> {
			brushController.setBrushRadius(brushSize.getValue());
			valLabel.setText(String.valueOf(brushSize.getValue()));
		});
		brushSizeOptions = new JPanel();
		brushSizeOptions.setLayout(new MigLayout("insets 4pt, gap 2pt, wmax 150"));
		JLabel label = new JLabel("Brush size:");
		label.setOpaque(true);
		label.setBackground(optionsBg);
		brushSize.setOpaque(false);
		brushSizeOptions.add(label, "grow");
		brushSizeOptions.add(valLabel, "right, wrap");
		brushSizeOptions.add(brushSize, "growx, spanx");
		brushSizeOptions.setBackground(optionsBg);
		brushSizeOptions.setBorder(BorderFactory.createLineBorder(optionsBorder));
		panel.add(brushSizeOptions, "al left, growy");
	}

	private JRadioButton createRadioButton(String name, String toolTip) {
		JRadioButton button = new JRadioButton(name);
		button.setToolTipText(toolTip);
		button.setMargin(new Insets(0, 0, 0, 0));
		return button;
	}

	private void setBrushSizeOptionVisible(boolean visible) {
		brushSizeOptions.setVisible(visible);
	}

	private void setEraseOptionsVisible(boolean visible) {
		eraseOptions.setVisible(visible);
	}

	private void updateOptionsPane(boolean brushSizeVisible,
		boolean eraseVisible)
	{
		setBrushSizeOptionVisible(brushSizeVisible);
		setEraseOptionsVisible(eraseVisible);
	}

	private void showLabelCursor() {
		bdvPanel.getDisplay().addMouseListener(brushMotionDrawer);
		bdvPanel.getDisplay().addMouseMotionListener(brushMotionDrawer);
	}

	private void hideLabelCursor() {
		bdvPanel.getDisplay().removeMouseListener(brushMotionDrawer);
		bdvPanel.getDisplay().removeMouseMotionListener(brushMotionDrawer);
	}

	private abstract class LabkitBtnListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent ev) {
			if (ev.getStateChange() == ItemEvent.SELECTED) {
				enableAction();
			}
			else if (ev.getStateChange() == ItemEvent.DESELECTED) {
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
			showLabelCursor();
		}

		@Override
		protected void disableAction() {
			updateOptionsPane(false, false);
			hideLabelCursor();
		}
	}

	private class EraseBtnListener extends LabkitBtnListener {

		@Override
		protected void enableAction() {
			addBindings(drawEraseMode ? "erase" : "floodclear", "button1");
			updateOptionsPane(drawEraseMode, true);
			if (drawEraseMode) showLabelCursor();
		}

		@Override
		protected void disableAction() {
			updateOptionsPane(false, false);
			hideLabelCursor();
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
			hideLabelCursor();
		}

		@Override
		protected void disableAction() {
			drawEraseMode = true;
			updateOptionsPane(true, true);
			addBindings("erase", "button1");
			showLabelCursor();
		}
	}

}
