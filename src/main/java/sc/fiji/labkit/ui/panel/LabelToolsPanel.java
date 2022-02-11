/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

import bdv.util.BdvHandle;
import bdv.viewer.ViewerPanel;
import sc.fiji.labkit.ui.brush.FloodFillController;
import sc.fiji.labkit.ui.brush.LabelBrushController;
import sc.fiji.labkit.ui.brush.SelectLabelController;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.*;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.util.EnumSet;

/**
 * Panel with the tool buttons for brush, flood fill, etc...
 */
public class LabelToolsPanel extends JPanel {

	private static final String BUTTON_BEHAVIOUR_ID = "panel";
	private static final Color OPTIONS_BORDER = new Color(220, 220, 220);
	private static final Color OPTIONS_BACKGROUND = new Color(230, 230, 230);

	private static final EnumSet<Mode> BRUSH_MODES = EnumSet.of(Mode.PAINT,
		Mode.ERASE);

	private final TriggerBehaviourBindings triggerBindings;

	private final FloodFillController floodFillController;
	private final LabelBrushController brushController;
	private final SelectLabelController selectLabelController;

	private JPanel brushOptionsPanel;
	private final MouseAdapter brushMotionDrawer;
	private final ViewerPanel bdvPanel;
	private final ButtonGroup group = new ButtonGroup();

	public LabelToolsPanel(BdvHandle bdvHandle,
		LabelBrushController brushController,
		FloodFillController floodFillController,
		SelectLabelController selectLabelController)
	{
		this.brushController = brushController;
		this.floodFillController = floodFillController;
		this.selectLabelController = selectLabelController;
		triggerBindings = bdvHandle.getTriggerbindings();
		bdvPanel = bdvHandle.getViewerPanel();

		setLayout(new MigLayout("flowy, insets 0, gap 4pt, top", "[][][][][]",
			"[]push"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		initActionButtons();
		add(initOptionPanel(), "wrap, growy");
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
		addBindings(getBehaviourId(mode));
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
			case SELECT_LABEL:
				return selectLabelController.behaviour();
		}
		throw new AssertionError();
	}

	private void setVisibility(Mode mode) {
		boolean brushVisible = BRUSH_MODES.contains(mode);
		if (brushOptionsPanel != null) brushOptionsPanel.setVisible(brushVisible);
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
		JCheckBox checkBox = new JCheckBox("allow overlapping labels");
		checkBox.setOpaque(false);
		checkBox.addItemListener(action -> {
			boolean overlapping = action.getStateChange() == ItemEvent.SELECTED;
			brushController.setOverlapping(overlapping);
			floodFillController.setOverlapping(overlapping);
		});
		return checkBox;
	}

	private JSlider initBrushSizeSlider() {
		JSlider brushSize = new JSlider(1, 50, (int) brushController
			.getBrushDiameter());
		brushSize.setPaintTrack(true);
		brushSize.addChangeListener(e -> {
			brushController.setBrushDiameter(brushSize.getValue());
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

	private void showLabelCursor() {
		bdvPanel.getDisplay().addMouseListener(brushMotionDrawer);
		bdvPanel.getDisplay().addMouseMotionListener(brushMotionDrawer);
	}

	private void hideLabelCursor() {
		bdvPanel.getDisplay().removeMouseListener(brushMotionDrawer);
		bdvPanel.getDisplay().removeMouseMotionListener(brushMotionDrawer);
	}

	private void addBindings(Behaviour behaviour) {
		final BehaviourMap behaviourMap = new BehaviourMap();
		behaviourMap.put("label tool button1", behaviour);
		behaviourMap.put("drag rotate", new Behaviour() {});
		behaviourMap.put("2d drag rotate", new Behaviour() {});
		final InputTriggerMap inputTriggerMap = new InputTriggerMap();
		inputTriggerMap.put(InputTrigger.getFromString("button1"),
			"label tool button1");
		triggerBindings.addInputTriggerMap(BUTTON_BEHAVIOUR_ID, inputTriggerMap);
		triggerBindings.addBehaviourMap(BUTTON_BEHAVIOUR_ID, behaviourMap);
	}

	private void removeBindings() {
		triggerBindings.removeInputTriggerMap(BUTTON_BEHAVIOUR_ID);
		triggerBindings.removeBehaviourMap(BUTTON_BEHAVIOUR_ID);
	}

	private enum Mode {
			MOVE, PAINT, FLOOD_FILL, ERASE, SELECT_LABEL, FLOOD_ERASE
	}
}
