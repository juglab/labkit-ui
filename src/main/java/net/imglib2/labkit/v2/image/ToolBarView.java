
package net.imglib2.labkit.v2.image;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Panel with the tool buttons for brush, flood fill, etc...
 */
public class ToolBarView extends JPanel {

	private static final Color OPTIONS_BORDER = new Color(220, 220, 220);
	private static final Color OPTIONS_BACKGROUND = new Color(230, 230, 230);

	private static final EnumSet<Mode> BRUSH_MODES = EnumSet.of(Mode.PAINT, Mode.ERASE);

	private ToolBarModel model;

	private final List<ToolBarViewListener> listeners = new CopyOnWriteArrayList<>();

	private JPanel brushOptionsPanel;
	private final ButtonGroup group = new ButtonGroup();

	public ToolBarView(ToolBarModel model) {
		this.model = model;
		setLayout(new MigLayout("flowy, insets 0, gap 4pt, top", "[][][][][]",
			"[]push"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		initActionButtons();
		add(initOptionPanel(), "wrap, growy");
		setMode(Mode.MOVE);
	}

	private void setMode(Mode mode) {
		listeners.forEach(listener -> listener.setMode(mode));
		setVisibility(mode);
	}

	private void setVisibility(Mode mode) {
		boolean brushVisible = BRUSH_MODES.contains(mode);
		if (brushOptionsPanel != null) brushOptionsPanel.setVisible(brushVisible);
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
		addActionButton("Select Label (Shift)", Mode.SELECT_LABEL,
			"/images/pipette.png");
		moveBtn.doClick();
	}

	private JPanel initOptionPanel() {
		JPanel optionPane = new JPanel();
		optionPane.setLayout(new MigLayout("insets 0"));
		optionPane.add(initOverrideCheckBox());
		optionPane.add(initBrushOptionPanel(), "al left");
		optionPane.setBackground(OPTIONS_BACKGROUND);
		optionPane.setBorder(BorderFactory.createLineBorder(OPTIONS_BORDER));
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

	private JPanel initBrushOptionPanel() {
		brushOptionsPanel = new JPanel();
		brushOptionsPanel.setLayout(new MigLayout("insets 4pt, gap 2pt, wmax 300"));
		brushOptionsPanel.setOpaque(false);
		brushOptionsPanel.add(new JLabel("Brush size:"), "grow");
		JSlider brushSizeSlider = initBrushSizeSlider();
		brushOptionsPanel.add(brushSizeSlider, "grow");
		brushOptionsPanel.add(initSliderValueLabel(brushSizeSlider), "right");
		return brushOptionsPanel;
	}

	private JCheckBox initOverrideCheckBox() {
		JCheckBox checkBox = new JCheckBox("override");
		checkBox.setOpaque(false);
		checkBox.addItemListener(action -> {
			boolean override = action.getStateChange() == ItemEvent.SELECTED;
			listeners.forEach(listener -> listener.setOverrideFlag(override));
		});
		return checkBox;
	}

	private JSlider initBrushSizeSlider() {
		JSlider brushSize = new JSlider(1, 50, model.getBrushRadius());
		brushSize.setPaintTrack(true);
		brushSize.addChangeListener(e -> {
			listeners.forEach(listener -> listener.setBrushRadius(brushSize.getValue()));
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

	public enum Mode {
			MOVE, PAINT, FLOOD_FILL, ERASE, SELECT_LABEL, FLOOD_ERASE
	}
}
