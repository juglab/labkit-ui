
package net.imglib2.labkit;

import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.viewer.DisplayMode;
import net.imglib2.labkit.actions.ToggleVisibility;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.control.brush.ChangeLabel;
import net.imglib2.labkit.control.brush.FloodFillController;
import net.imglib2.labkit.control.brush.LabelBrushController;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.LabelToolsPanel;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;

public class BasicLabelingComponent implements AutoCloseable {

	private BdvHandle bdvHandle;

	private JPanel panel = new JPanel();

	private final JFrame dialogBoxOwner;

	private ActionsAndBehaviours actionsAndBehaviours;

	private ImageLabelingModel model;

	public JComponent getComponent() {
		return panel;
	}

	public BasicLabelingComponent(final JFrame dialogBoxOwner,
		final ImageLabelingModel model)
	{
		this.model = model;
		this.dialogBoxOwner = dialogBoxOwner;

		initBdv(model.spatialDimensions().numDimensions() < 3);
		actionsAndBehaviours = new ActionsAndBehaviours(bdvHandle);
		initLabelsLayer();
		initImageLayer();
		JPanel toolsPanel = initBrushLayer();
		initPanel(toolsPanel);
		this.model.transformationModel().initialize(bdvHandle.getViewerPanel());
	}

	private void initBdv(boolean is2D) {
		final BdvOptions options = BdvOptions.options();
		if (is2D) options.is2D();
		bdvHandle = new BdvHandlePanel(dialogBoxOwner, options);
		bdvHandle.getViewerPanel().setDisplayMode(DisplayMode.FUSED);
	}

	private void initPanel(JPanel toolsPanel) {
		panel.setLayout(new MigLayout("", "[grow]", "[][grow]"));
		panel.add(toolsPanel, "wrap, growx");
		panel.add(bdvHandle.getViewerPanel(), "grow");
	}

	private void initImageLayer() {
		addBdvLayer(new BdvLayer.FinalLayer(model.showable(), "Image"), model.imageVisibility());
	}

	private void initLabelsLayer() {
		addBdvLayer(new LabelsLayer(model), model.labelingVisibility());
	}

	public BdvSource addBdvLayer(BdvLayer layer, Holder<Boolean> visibility) {
		BdvOptions options = BdvOptions.options().addTo(bdvHandle);
		BdvSource source = layer.image().show(layer.title(), options);
		layer.listeners().add(this::requestRepaint);
		ToggleVisibility action = new ToggleVisibility(layer.title(), source);
		addAction(action);
		visibility.notifier().add(action::setVisible);
		action.addPropertyChangeListener(propertyChangeEvent -> {
			if(propertyChangeEvent.getPropertyName().equals(Action.SELECTED_KEY))
				visibility.set((Boolean) propertyChangeEvent.getNewValue());
		} );
		layer.makeVisible().add(() -> action.setVisible(true));
		return source;
	}

	private JPanel initBrushLayer() {
		final LabelBrushController brushController = new LabelBrushController(
			bdvHandle.getViewerPanel(), new BitmapModel(model), actionsAndBehaviours,
			model.isTimeSeries());
		final FloodFillController floodFillController = new FloodFillController(
			bdvHandle.getViewerPanel(), model, actionsAndBehaviours, model
				.isTimeSeries());
		JPanel toolsPanel = new LabelToolsPanel(bdvHandle, brushController,
			floodFillController);
		actionsAndBehaviours.addAction(new ChangeLabel(model));
		return toolsPanel;
	}

	public void addAction(AbstractNamedAction action) {
		actionsAndBehaviours.addAction(action);
	}

	private void requestRepaint() {
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public void close() {
		bdvHandle.close();
	}
}
