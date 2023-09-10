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

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;

import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import net.miginfocom.swing.MigLayout;
import sc.fiji.labkit.ui.LabKitKeymapManager;
import sc.fiji.labkit.ui.brush.FloodFillController;
import sc.fiji.labkit.ui.brush.LabelBrushController;
import sc.fiji.labkit.ui.brush.PlanarModeController;
import sc.fiji.labkit.ui.brush.SelectLabelController;

/**
 * Panel with the tool buttons for brush, flood fill, etc... Activates and
 * deactivates mouse behaviours.
 */
public class LabelToolsPanel extends JPanel {

	private static final Color OPTIONS_BORDER = new Color(220, 220, 220);
	private static final Color OPTIONS_BACKGROUND = new Color(230, 230, 230);

	private static final String MOVE_TOOL_TIP = "<html><b>Move</b><br>" +
		"<small>Keyboard shortcuts:<br>" +
		"- <b>Left Click</b> on the image and drag, to rotate the image.<br>" +
		"- <b>Right Click</b> on the image, to move around.<br>" +
		"- <b>Ctrl + Shift + Mouse Wheel</b> to zoom in and out.<br>" +
		"- <b>Mouse Wheel</b> only, to scroll through a 3d image.<br>" +
		"- Press <b>Ctrl + G</b> to deactivate drawing tools and activate this mode.</small></html>";
	private static final String DRAW_TOOL_TIP = "<html><b>Draw</b><br>" +
		"<small>Keyboard shortcuts:<br>" +
		"- Hold down the <b>D</b> key and <b>Left Click</b> on the image to draw.<br>" +
		"- Hold down the <b>D</b> key and use the <b>Mouse Wheel</b> to change the brush diameter.<br>" +
		"- Or press <b>Ctrl + D</b> to activate the drawing tool.</small></html>";
	private static final String ERASE_TOOL_TIP = "<html><b>Erase</b><br>" +
		"<small>Keyboard shortcuts:<br>" +
		"- Hold down the <b>E</b> key and <b>Left Click</b> on the image to erase.<br>" +
		"- Hold down the <b>E</b> key and use the <b>Mouse Wheel</b> to change the brush diameter.<br>" +
		"- Or press <b>Ctrl + E</b> to activate the eraser tool.</small></html>";
	private static final String FLOOD_FILL_TOOL_TIP = "<html><b>Flood Fill</b><br>" +
		"<small>Keyboard shortcuts:<br>" +
		"- Hold down the <b>F</b> key and <b>Left Click</b> on the image to flood fill.<br>" +
		"- Or press <b>Ctrl + F</b> to activate the flood fill tool.</small></html>";
	private static final String FLOOD_ERASE_TOOL_TIP = "<html><b>Remove Connected Component</b><br>" +
		"<small>Keyboard shortcuts:<br>" +
		"- Hold down the <b>R</b> key and <b>Left Click</b> on the image to remove connected component.<br>" +
		"- Or press <b>Ctrl + R</b> to activate the remove connected component tool.</small></html>";
	private static final String SELECT_LABEL_TOOL_TIP = "<html><b>Select Label</b><br>" +
		"<small>Keyboard shortcuts:<br>" +
		"- Hold down the <b>Shift</b> key and <b>Left Click</b> on the image<br>" +
		"  to select the label under the cursor.</small></html>";

	private static final String TOGGLE_PLANAR_MODE_ACTION = "toggle planar mode";
	private static final String TOGGLE_MOVE_MODE_ACTION = "move mode";
	private static final String TOGGLE_DRAW_MODE_ACTION = "draw mode";
	private static final String TOGGLE_FLOOD_FILL_MODE_ACTION = "flood fill mode";
	private static final String TOGGLE_ERASE_MODE_ACTION = "erase mode";
	private static final String TOGGLE_FLOOD_ERASE_MODE_ACTION = "remove connected component mode";
	private static final String TOGGLE_SELECT_LABEL_MODE_ACTION = "select label mode";

	private static final String[] TOGGLE_PLANAR_MODE_KEYS = new String[] { "not mapped" };
	private static final String[] TOGGLE_MOVE_MODE_KEYS = new String[] { "ctrl G" };
	private static final String[] TOGGLE_DRAW_MODE_KEYS = new String[] { "ctrl D" };
	private static final String[] TOGGLE_FLOOD_FILL_MODE_KEYS = new String[] { "ctrl F" };
	private static final String[] TOGGLE_ERASE_MODE_KEYS = new String[] { "ctrl E" };
	private static final String[] TOGGLE_FLOOD_ERASE_MODE_KEYS = new String[] { "ctrl R" };
	private static final String[] TOGGLE_SELECT_LABEL_MODE_KEYS = new String[] { "not mapped" };

	private static final String TOGGLE_PLANAR_MODE_DESCRIPTION = "Toggle between slice-by-slice editing and 3d editing.";
	private static final String TOGGLE_MOVE_MODE_DESCRIPTION = "Activate the move mode. Clicking and dragging will move the view.";
	private static final String TOGGLE_DRAW_MODE_DESCRIPTION = "Activate the draw mode. Clicking and dragging will draw the currently selected label on the annotation layer.";
	private static final String TOGGLE_FLOOD_FILL_MODE_DESCRIPTION = "Activate the flood-fill mode. Clicking inside a connected component or in a closed region in the background will repaint it with the currently selected label.";
	private static final String TOGGLE_ERASE_MODE_DESCRIPTION = "Activate the eraser mode. Clicking and dragging will erase any label under the mouse.";
	private static final String TOGGLE_FLOOD_ERASE_MODE_DESCRIPTION = "Activate the remove-connected-component mode. Clicking inside a connected component will erase its label.";
	private static final String TOGGLE_SELECT_LABEL_MODE_DESCRIPTION = "Activate the select-label mode. Clicking on a pixel will select the label it has, if any.";

	private final FloodFillController floodFillController;
	private final LabelBrushController brushController;
	private final SelectLabelController selectLabelController;
	private final PlanarModeController planarModeController;

	private JPanel brushOptionsPanel;
	private final ButtonGroup group = new ButtonGroup();

	private Mode mode = ignore -> {};
	private AbstractNamedAction togglePlanarModeAction;
	private DoClickButtonAction toggleMoveMode;
	private DoClickButtonAction toggleDrawMode;
	private DoClickButtonAction toggleFloodFillMode;
	private DoClickButtonAction toggleEraseMode;
	private DoClickButtonAction toggleFloodEraseMode;
	private DoClickButtonAction toggleSelectLabelMode;

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
		JToggleButton moveBtn = addActionButton(MOVE_TOOL_TIP, ignore -> {}, false,
			"/images/move.png", "MOVE_TOOL", "ctrl G");
		JToggleButton drawBtn = addActionButton(DRAW_TOOL_TIP,
			brushController::setBrushActive, true,
			"/images/draw.png", "DRAW_TOOL", "ctrl D");
		JToggleButton floodFillBtn = addActionButton(FLOOD_FILL_TOOL_TIP,
			floodFillController::setFloodFillActive, false,
			"/images/fill.png", "FILL_TOOL", "ctrl F");
		JToggleButton eraseBtn = addActionButton(ERASE_TOOL_TIP,
			brushController::setEraserActive, true,
			"/images/erase.png", "ERASE_TOOL", "ctrl E");
		JToggleButton floodEraseBtn = addActionButton(FLOOD_ERASE_TOOL_TIP,
			floodFillController::setRemoveBlobActive, false,
			"/images/flooderase.png", "FLOOD_ERASE_TOOL", "ctrl R");
		JToggleButton selectLabelBtn = addActionButton(SELECT_LABEL_TOOL_TIP,
			selectLabelController::setActive, false,
			"/images/pipette.png");
		moveBtn.doClick();
		
		this.toggleMoveMode = new DoClickButtonAction(moveBtn, TOGGLE_MOVE_MODE_ACTION);
		this.toggleDrawMode = new DoClickButtonAction(drawBtn, TOGGLE_DRAW_MODE_ACTION);
		this.toggleFloodFillMode = new DoClickButtonAction(floodFillBtn, TOGGLE_FLOOD_FILL_MODE_ACTION);
		this.toggleEraseMode = new DoClickButtonAction(eraseBtn, TOGGLE_ERASE_MODE_ACTION);
		this.toggleFloodEraseMode = new DoClickButtonAction(floodEraseBtn, TOGGLE_FLOOD_ERASE_MODE_ACTION);
		this.toggleSelectLabelMode = new DoClickButtonAction(selectLabelBtn, TOGGLE_SELECT_LABEL_MODE_ACTION);
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
		this.togglePlanarModeAction = createTogglePlanarModeAction(button);
		ImageIcon rotateIcon = getIcon("/images/rotate.png");
		ImageIcon planarIcon = getIcon("/images/planes.png");
		button.setIcon(rotateIcon);
		button.setFocusable(false);
		String ENABLE_TEXT = "Click to: Enable slice by slice editing of 3d images.";
		String DISABLE_TEXT = "Click to: Disable slice by slice editing and freely rotate 3d images.";
		button.addItemListener(ignore -> {
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

	private AbstractNamedAction createTogglePlanarModeAction(JToggleButton button) {
		return new AbstractNamedAction(TOGGLE_PLANAR_MODE_ACTION) {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				button.setSelected(!button.isSelected());
			}
		};
	}

	private JToggleButton addActionButton(String toolTipText, Mode mode, boolean visibility,
		String iconPath, String activationKey, String key)
	{
		JToggleButton button = addActionButton(toolTipText, mode, visibility, iconPath);
		button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key),
			activationKey);
		button.getActionMap().put(activationKey, new RunnableAction(activationKey, button::doClick));
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

	public void install(Actions actions) {
		actions.namedAction(togglePlanarModeAction, TOGGLE_PLANAR_MODE_KEYS);
		actions.namedAction(toggleMoveMode, TOGGLE_MOVE_MODE_KEYS);
		actions.namedAction(toggleDrawMode, TOGGLE_DRAW_MODE_KEYS);
		actions.namedAction(toggleEraseMode, TOGGLE_ERASE_MODE_KEYS);
		actions.namedAction(toggleFloodFillMode, TOGGLE_FLOOD_FILL_MODE_KEYS);
		actions.namedAction(toggleFloodEraseMode, TOGGLE_FLOOD_ERASE_MODE_KEYS);
		actions.namedAction(toggleSelectLabelMode, TOGGLE_SELECT_LABEL_MODE_KEYS);
	}

	/**
	 * Named action that simply clicks on a specified button.
	 */
	private final static class DoClickButtonAction extends AbstractNamedAction {

		private static final long serialVersionUID = 1L;
		private AbstractButton button;

		public DoClickButtonAction(AbstractButton button, String name) {
			super(name);
			this.button = button;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			button.doClick();
		}
	}

	@Plugin(type = CommandDescriptionProvider.class)
	public static class Descriptions extends CommandDescriptionProvider {
		public Descriptions() {
			super(LabKitKeymapManager.LABKIT_SCOPE, LabKitKeymapManager.LABKIT_CONTEXT);
		}

		@Override
		public void getCommandDescriptions(final CommandDescriptions descriptions) {
			descriptions.add(TOGGLE_PLANAR_MODE_ACTION, TOGGLE_PLANAR_MODE_KEYS, TOGGLE_PLANAR_MODE_DESCRIPTION);
			descriptions.add(TOGGLE_MOVE_MODE_ACTION, TOGGLE_MOVE_MODE_KEYS, TOGGLE_MOVE_MODE_DESCRIPTION);
			descriptions.add(TOGGLE_DRAW_MODE_ACTION, TOGGLE_DRAW_MODE_KEYS, TOGGLE_DRAW_MODE_DESCRIPTION);
			descriptions.add(TOGGLE_ERASE_MODE_ACTION, TOGGLE_ERASE_MODE_KEYS, TOGGLE_ERASE_MODE_DESCRIPTION);
			descriptions.add(TOGGLE_FLOOD_FILL_MODE_ACTION, TOGGLE_FLOOD_FILL_MODE_KEYS,
					TOGGLE_FLOOD_FILL_MODE_DESCRIPTION);
			descriptions.add(TOGGLE_FLOOD_ERASE_MODE_ACTION, TOGGLE_FLOOD_ERASE_MODE_KEYS,
					TOGGLE_FLOOD_ERASE_MODE_DESCRIPTION);
			descriptions.add(TOGGLE_SELECT_LABEL_MODE_ACTION, TOGGLE_SELECT_LABEL_MODE_KEYS,
					TOGGLE_SELECT_LABEL_MODE_DESCRIPTION);
		}
	}
}
