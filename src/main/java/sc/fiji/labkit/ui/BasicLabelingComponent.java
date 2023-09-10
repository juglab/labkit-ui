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

package sc.fiji.labkit.ui;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.KeymapManager;
import bdv.ui.splitpanel.SplitPanel;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.NavigationActions;
import bdv.viewer.ViewerPanel;
import net.miginfocom.swing.MigLayout;
import sc.fiji.labkit.ui.bdv.BdvAutoContrast;
import sc.fiji.labkit.ui.bdv.BdvLayer;
import sc.fiji.labkit.ui.bdv.BdvLayerLink;
import sc.fiji.labkit.ui.brush.ChangeLabel;
import sc.fiji.labkit.ui.brush.FloodFillController;
import sc.fiji.labkit.ui.brush.LabelBrushController;
import sc.fiji.labkit.ui.brush.PlanarModeController;
import sc.fiji.labkit.ui.brush.SelectLabelController;
import sc.fiji.labkit.ui.labeling.LabelsLayer;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.panel.LabelToolsPanel;

/**
 * A swing UI component that shows a Big Data Viewer panel and a tool bar for
 * label editing.
 */
public class BasicLabelingComponent extends JPanel implements AutoCloseable {

	private final Holder<BdvStackSource<?>> imageSource;

	private BdvHandle bdvHandle;

	private final JFrame dialogBoxOwner;

	private ActionsAndBehaviours actionsAndBehaviours;

	private ImageLabelingModel model;

	private JSlider zSlider;

	private KeymapManager keymapManager;

	public BasicLabelingComponent(final JFrame dialogBoxOwner,
		final ImageLabelingModel model)
	{
		this(dialogBoxOwner, model, null);
	}

	public BasicLabelingComponent(final JFrame dialogBoxOwner, final ImageLabelingModel model,
			KeymapManager keymapManager) {
		this.model = model;
		this.dialogBoxOwner = dialogBoxOwner;
		this.keymapManager = keymapManager;

		initBdv(model.spatialDimensions().numDimensions() < 3);
		actionsAndBehaviours = new ActionsAndBehaviours(bdvHandle);
		this.imageSource = initImageLayer();
		initLabelsLayer();
		initPanel();
		this.model.transformationModel().initialize(bdvHandle.getViewerPanel());
	}

	private void initBdv(boolean is2D) {
		final BdvOptions options = BdvOptions.options();
		if (is2D) options.is2D();
		if (keymapManager != null) options.keymapManager(keymapManager);

		bdvHandle = new BdvHandlePanel(dialogBoxOwner, options);
		bdvHandle.getViewerPanel().setDisplayMode(DisplayMode.FUSED);

		if (keymapManager != null) {
			ViewerPanel viewer = bdvHandle.getViewerPanel();

			InputActionBindings keybindings = bdvHandle.getKeybindings();
			TriggerBehaviourBindings triggerbindings = bdvHandle.getTriggerbindings();

			Keymap keymap = keymapManager.getForwardSelectedKeymap();

			final Actions actions = new Actions(keymap.getConfig(), "bdv");
			actions.install(keybindings, "view");

			Behaviours behaviours = new Behaviours(keymap.getConfig(), "bdv");
			behaviours.install(triggerbindings, "view");

			viewer.getTransformEventHandler().install(behaviours);
			NavigationActions.install(actions, viewer, is2D);

			keymap.updateListeners().add(() -> {
				actions.updateKeyConfig(keymap.getConfig());
				behaviours.updateKeyConfig(keymap.getConfig());
			});
		}
	}

	private void initPanel() {
		setLayout(new MigLayout("", "[grow]", "[][grow]"));
		zSlider = new JSlider(Adjustable.VERTICAL);
		zSlider.setFocusable(false);
		add(initToolsPanel(), "wrap, growx");
		JPanel bdvAndZSlider = new JPanel();
		bdvAndZSlider.setLayout(new BorderLayout());
		bdvAndZSlider.add(bdvHandle.getSplitPanel());
		bdvAndZSlider.add(zSlider, BorderLayout.LINE_END);
		add(bdvAndZSlider, "grow");
	}

	private Holder<BdvStackSource<?>> initImageLayer() {
		return addBdvLayer(new BdvLayer.FinalLayer(model.showable(), "Image", model
			.imageVisibility()));
	}

	private void initLabelsLayer() {
		addBdvLayer(new LabelsLayer(model));
	}

	public Holder<BdvStackSource<?>> addBdvLayer(BdvLayer layer) {
		return new BdvLayerLink(layer, bdvHandle);
	}

	private JPanel initToolsPanel() {

		final PlanarModeController planarModeController = new PlanarModeController(
			bdvHandle, model, zSlider);
		final LabelBrushController brushController = new LabelBrushController(
			bdvHandle, model, actionsAndBehaviours);
		final FloodFillController floodFillController = new FloodFillController(
			bdvHandle, model, actionsAndBehaviours);
		final SelectLabelController selectLabelController =
			new SelectLabelController(bdvHandle, model, actionsAndBehaviours);
		final LabelToolsPanel toolsPanel = new LabelToolsPanel(brushController,
			floodFillController, selectLabelController, planarModeController);
		actionsAndBehaviours.addAction(new ChangeLabel(model));
		
		if (keymapManager != null) {
			InputActionBindings keybindings = bdvHandle.getKeybindings();
			TriggerBehaviourBindings triggerbindings = bdvHandle.getTriggerbindings();
			
			Keymap keymap = keymapManager.getForwardSelectedKeymap();
			
			final Actions actions = new Actions(keymap.getConfig(), "labkit");
			actions.install(keybindings, "annotating");
			
			Behaviours behaviours = new Behaviours(keymap.getConfig(), "labkit");
			behaviours.install(triggerbindings, "annotating");
			
			keymap.updateListeners().add(() -> {
				actions.updateKeyConfig(keymap.getConfig());
				behaviours.updateKeyConfig(keymap.getConfig());
			});

			toolsPanel.install(actions);
		}
		return toolsPanel;
	}

	public void addShortcuts(
		Collection<? extends AbstractNamedAction> shortcuts)
	{
		shortcuts.forEach(actionsAndBehaviours::addAction);
	}

	@Override
	public void close() {
		bdvHandle.close();
	}

	public void autoContrast() {
		BdvAutoContrast.autoContrast(imageSource.get());
	}

	public void toggleContrastSettings() {
		SplitPanel splitPanel = bdvHandle.getSplitPanel();
		splitPanel.setCollapsed(!splitPanel.isCollapsed());
	}

}
