
package net.imglib2.labkit.panel;

import bdv.util.BdvHandle;
import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.control.brush.FloodFillController;
import net.imglib2.labkit.control.brush.LabelBrushController;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.*;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.util.EnumSet;

public class LabelToolsPanel extends JPanel {

	private static final String BUTTON_BEHAVIOUR_ID = "panel";
	private static final Color OPTIONS_BORDER = new Color(220, 220, 220);
	private static final Color OPTIONS_BACKGROUND = new Color(230, 230, 230);

	private static final EnumSet<Mode> BRUSH_MODES = EnumSet.of(Mode.PAINT,
		Mode.ERASE);

	private final TriggerBehaviourBindings triggerBindings;

	private final FloodFillController floodFillController;
	private final LabelBrushController brushController;

	private JPanel brushSizeOptions;
	private final JPanel optionPane;
	private final MouseAdapter brushMotionDrawer;
	private final ViewerPanel bdvPanel;
	private final ButtonGroup group = new ButtonGroup();

	public LabelToolsPanel(BdvHandle bdvHandle,
		LabelBrushController brushController,
		FloodFillController floodFillController)
	{
		this.brushController = brushController;
		this.floodFillController = floodFillController;
		triggerBindings = bdvHandle.getTriggerbindings();
		bdvPanel = bdvHandle.getViewerPanel();

		setLayout(new MigLayout("flowy, insets 0, gap 4pt, top", "[][][][][]",
			"[]push"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		optionPane = initOptionPane();
		initActionButtons();
		add(optionPane, "wrap, growy");
		brushMotionDrawer = GuiUtils.toMouseListener(brushController
			.drawBrushBehaviour());
		setMode(Mode.MOVE);
	}

	private void setMode(Mode mode) {
		setVisibility(mode);
		setBindings(mode);
	}

	private void setBindings(Mode mode) {
		removeBindings();
		if (mode == Mode.MOVE) return;
		addBindings(getBehaviourId(mode), "button1");
	}

	private Behaviour getBehaviourId(Mode mode) {
		switch (mode) {
			case MOVE:
				throw new AssertionError(); // there is no behavior for Mode.MOVE
			case PAINT:
				return brushController.paintBehaviour();
			case FLOOD_FILL:
				return floodFillController.floodFillBehaviour();
			case ERASE:
				return brushController.eraseBehaviour();
			case FLOOD_ERASE:
				return floodFillController.floodEraseBehaviour();
		}
		throw new AssertionError();
	}

	private void setVisibility(Mode mode) {
		boolean brushVisible = BRUSH_MODES.contains(mode);
		brushSizeOptions.setVisible(brushVisible);
		if (brushVisible) showLabelCursor();
		else hideLabelCursor();
	}

	private void initActionButtons() {
		JToggleButton moveBtn = addActionButton("Move", Mode.MOVE,
			"/images/move.png");
		addActionButton("Draw (D)", Mode.PAINT, "/images/draw.png");
		addActionButton("Flood Fill (F)\nThis only works properly on 2D images",
			Mode.FLOOD_FILL, "/images/fill.png");
		addActionButton("Erase (E)", Mode.ERASE, "/images/erase.png");
		addActionButton("Remove Blob (R)", Mode.FLOOD_ERASE,
			"/images/flooderase.png");
		moveBtn.doClick();
	}

	private JPanel initOptionPane() {
		JPanel optionPane = new JPanel();
		optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.LINE_AXIS));
		optionPane.add(Box.createRigidArea(new Dimension(5, 0)));
		addBrushSizeOption(optionPane);
		return optionPane;
	}

	private JToggleButton addActionButton(String toolTipText, Mode mode,
		String iconPath)
	{
		JToggleButton button = new JToggleButton();
		button.setIcon(new ImageIcon(this.getClass().getResource(iconPath)));
		button.setToolTipText(toolTipText);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addItemListener(ev -> {
			if (ev.getStateChange() == ItemEvent.SELECTED) {
				setMode(mode);
			}
		});
		group.add(button);
		add(button, "wrap, top");
		return button;
	}

	private void addBrushSizeOption(JPanel panel) {
		JSlider brushSize = initBrushSizeSlider();
		JLabel valLabel = initSliderValueLabel(brushSize);
		JLabel label = initBrushSizeLabel();
		initBrushSizeOptionPanel(brushSize, valLabel, label);
		panel.add(brushSizeOptions, "al left");
	}

	private JSlider initBrushSizeSlider() {
		JSlider brushSize = new JSlider(1, 50, (int) brushController
			.getBrushRadius());
		brushSize.setPaintTrack(true);
		brushSize.addChangeListener(e -> {
			brushController.setBrushRadius(brushSize.getValue());
		});
		brushSize.setOpaque(false);
		return brushSize;
	}

	private JLabel initSliderValueLabel(JSlider brushSize) {
		JLabel valLabel = new JLabel(String.valueOf(brushSize.getValue()));
		brushSize.addChangeListener(e -> {
			valLabel.setText(String.valueOf(brushSize.getValue()));
		});
		return valLabel;
	}

	private JLabel initBrushSizeLabel() {
		JLabel label = new JLabel("Brush size:");
		label.setOpaque(true);
		label.setBackground(OPTIONS_BACKGROUND);
		return label;
	}

	private void initBrushSizeOptionPanel(JSlider brushSize, JLabel valLabel,
		JLabel label)
	{
		brushSizeOptions = new JPanel();
		brushSizeOptions.setLayout(new MigLayout("insets 4pt, gap 2pt, wmax 200"));
		brushSizeOptions.add(label, "grow");
		brushSizeOptions.add(brushSize, "grow");
		brushSizeOptions.add(valLabel, "right");
		brushSizeOptions.setBackground(OPTIONS_BACKGROUND);
		brushSizeOptions.setBorder(BorderFactory.createLineBorder(OPTIONS_BORDER));
	}

	private JRadioButton createRadioButton(String name, String toolTip) {
		JRadioButton button = new JRadioButton(name);
		button.setToolTipText(toolTip);
		button.setMargin(new Insets(0, 0, 0, 0));
		return button;
	}

	private void showLabelCursor() {
		bdvPanel.getDisplay().addMouseListener(brushMotionDrawer);
		bdvPanel.getDisplay().addMouseMotionListener(brushMotionDrawer);
	}

	private void hideLabelCursor() {
		bdvPanel.getDisplay().removeMouseListener(brushMotionDrawer);
		bdvPanel.getDisplay().removeMouseMotionListener(brushMotionDrawer);
	}

	private void addBindings(Behaviour behaviour, String trigger) {
		final BehaviourMap behaviourMap = new BehaviourMap();
		behaviourMap.put("label tool button1", behaviour);
		behaviourMap.put("drag rotate", new Behaviour() {});
		behaviourMap.put("2d drag rotate", new Behaviour() {});
		final InputTriggerMap inputTriggerMap = new InputTriggerMap();
		inputTriggerMap.put(InputTrigger.getFromString(trigger),
			"label tool button1");
		triggerBindings.addInputTriggerMap(BUTTON_BEHAVIOUR_ID, inputTriggerMap);
		triggerBindings.addBehaviourMap(BUTTON_BEHAVIOUR_ID, behaviourMap);
	}

	private void removeBindings() {
		triggerBindings.removeInputTriggerMap(BUTTON_BEHAVIOUR_ID);
		triggerBindings.removeBehaviourMap(BUTTON_BEHAVIOUR_ID);
	}

	private enum Mode {
			MOVE, PAINT, FLOOD_FILL, ERASE, FLOOD_ERASE
	}
}
