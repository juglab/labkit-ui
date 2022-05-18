/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2022 Matthias Arzt
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

import sc.fiji.labkit.ui.brush.FloodFillController;
import sc.fiji.labkit.ui.brush.LabelBrushController;
import sc.fiji.labkit.ui.brush.PlanarModeController;
import sc.fiji.labkit.ui.brush.SelectLabelController;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

/**
 * Panel with the tool buttons for brush, flood fill, etc... Activates and
 * deactivates mouse behaviours.
 */
public class LabelToolsPanel extends JPanel {

	private static final Color OPTIONS_BORDER = new Color(220, 220, 220);
	private static final Color OPTIONS_BACKGROUND = new Color(230, 230, 230);

	private final FloodFillController floodFillController;
	private final LabelBrushController brushController;
	private final SelectLabelController selectLabelController;
	private final PlanarModeController planarModeController;

	private JPanel brushOptionsPanel;
	private final ButtonGroup group = new ButtonGroup();

	private Mode mode = ignore -> {};

	public LabelToolsPanel(LabelBrushController brushController,
		FloodFillController floodFillController, SelectLabelController selectLabelController,
		PlanarModeController planarModeController)
	{
		this.brushController = brushController;
		this.floodFillController = floodFillController;
		this.selectLabelController = selectLabelController;
		this.planarModeController = planarModeController;

		setLayout(new MigLayout("flowy, insets 0, gap 4pt, top", "[][][][][]",
			"[]push"));
		setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		initActionButtons();
		add(initOptionPanel(), "wrap, growy");
		add(initPlanarModeButton(), "growy");
	}

	private void setMode(Mode mode) {
		this.mode.setActive(false);
		this.mode = mode;
		this.mode.setActive(true);
	}

	private void setVisibility(boolean brushVisible) {
		if (brushOptionsPanel != null) brushOptionsPanel.setVisible(brushVisible);
	}

	private void initActionButtons() {
		JToggleButton moveBtn = addActionButton("Move", ignore -> {}, false,
			"/images/move.png");
		addActionButton("Draw (D)",
			brushController::setBrushActive, true,
			"/images/draw.png");
		addActionButton("Flood Fill (F)\nThis only works properly on 2D images",
			floodFillController::setFloodFillActive, false,
			"/images/fill.png");
		addActionButton("Erase (E)",
			brushController::setEraserActive, true,
			"/images/erase.png");
		addActionButton("Remove Blob (R)",
			floodFillController::setRemoveBlobActive, false,
			"/images/flooderase.png");
		addActionButton("Select Label (Shift)",
			selectLabelController::setActive, false,
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

	private JToggleButton initPlanarModeButton() {
		JToggleButton button = new JToggleButton();
		ImageIcon rotateIcon = getIcon("/images/rotate.png");
		ImageIcon planarIcon = getIcon("/images/planes.png");
		button.setIcon(rotateIcon);
		button.setFocusable(false);
		String ENABLE_TEXT = "Click to: Enable slice by slice editing of 3d images.";
		String DISABLE_TEXT = "Click to: Disable slice by slice editing and freely rotate 3d images.";
		button.addActionListener(ignore -> {
			boolean selected = button.isSelected();
			button.setIcon(selected ? planarIcon : rotateIcon);
			button.setToolTipText(selected ? DISABLE_TEXT : ENABLE_TEXT);
			planarModeController.setActive(selected);
			floodFillController.setPlanarMode(selected);
			brushController.setPlanarMode(selected);
		});
		button.setToolTipText(ENABLE_TEXT);
		return button;
	}

	private JToggleButton addActionButton(String toolTipText, Mode mode, boolean visibility,
		String iconPath)
	{
		JToggleButton button = new JToggleButton();
		button.setIcon(getIcon(iconPath));
		button.setToolTipText(toolTipText);
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setFocusable(false);
		button.addItemListener(ev -> {
			if (ev.getStateChange() == ItemEvent.SELECTED) {
				setMode(mode);
				setVisibility(visibility);
			}
		});
		group.add(button);
		add(button, "wrap, top");
		return button;
	}

	private ImageIcon getIcon(String iconPath) {
		return new ImageIcon(this.getClass().getResource(iconPath));
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
		brushSize.setFocusable(false);
		brushSize.setPaintTrack(true);
		brushSize.addChangeListener(e -> {
			brushController.setBrushDiameter(brushSize.getValue());
		});
		brushSize.setOpaque(false);
		brushController.brushDiameterListeners().addListener(() -> {
			double diameter = brushController.getBrushDiameter();
			brushSize.setValue((int) diameter);
		});
		return brushSize;
	}

	private JLabel initSliderValueLabel(JSlider brushSize) {
		JLabel valLabel = new JLabel(String.valueOf(brushSize.getValue()));
		brushSize.addChangeListener(e -> {
			valLabel.setText(String.valueOf(brushSize.getValue()));
		});
		return valLabel;
	}

	private interface Mode {

		void setActive(boolean active);
	}
}
