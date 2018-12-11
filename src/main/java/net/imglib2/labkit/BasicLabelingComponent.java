
package net.imglib2.labkit;

import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.viewer.DisplayMode;
import net.imglib2.labkit.actions.ToggleVisibility;
import net.imglib2.labkit.bdv.BdvAutoContrast;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.brush.ChangeLabel;
import net.imglib2.labkit.brush.FloodFillController;
import net.imglib2.labkit.brush.LabelBrushController;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.LabelToolsPanel;
import net.imglib2.type.numeric.ARGBType;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.util.Collection;

public class BasicLabelingComponent implements AutoCloseable {

	private final BdvSource imageSource;

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
		this.imageSource = initImageLayer();
		initLabelsLayer();
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

	private BdvSource initImageLayer() {
		return addBdvLayer(new BdvLayer.FinalLayer(model.showable(), "Image", model
			.imageVisibility()));
	}

	private void initLabelsLayer() {
		addBdvLayer(new LabelsLayer(model));
	}

	public BdvSource addBdvLayer(BdvLayer layer) {
		final BdvSource bdvSource = addToBdv(bdvHandle, layer);
		ToggleVisibility action = new ToggleVisibility(layer.title(), bdvSource);
		actionsAndBehaviours.addAction(action);
		layer.visibility().notifier().add(action::setVisible);
		action.addPropertyChangeListener(propertyChangeEvent -> {
			if (propertyChangeEvent.getPropertyName().equals(Action.SELECTED_KEY))
				layer.visibility().set((Boolean) propertyChangeEvent.getNewValue());
		});
		return bdvSource;
	}

	private static BdvSource addToBdv(BdvHandle bdvHandle, BdvLayer layer) {
		ForwardingSource source = new ForwardingSource(bdvHandle);
		BdvOptions options = BdvOptions.options().addTo(bdvHandle);
		source.setSource(layer.image().get().show(layer.title(), options));
		layer.image().notifier().add(showable -> {
			source.removeFromBdv();
			source.setSource(layer.image().get().show(layer.title(), options));
		});
		layer.listeners().add(bdvHandle.getViewerPanel()::requestRepaint);
		return source;
	}

	private static class ForwardingSource extends BdvSource {

		private BdvSource source = null;

		public ForwardingSource(BdvHandle handle) {
			super(handle, 1);
		}

		public void setSource(BdvSource source) {
			this.source = source;
		}

		@Override
		public void close() {
			if (source != null) source.close();
		}

		@Override
		public void removeFromBdv() {
			if (source != null) source.removeFromBdv();
		}

		@Override
		public void setDisplayRange(double min, double max) {
			if (source != null) source.setDisplayRange(min, max);
		}

		@Override
		public void setDisplayRangeBounds(double min, double max) {
			if (source != null) source.setDisplayRangeBounds(min, max);
		}

		@Override
		public void setColor(ARGBType color) {
			if (source != null) source.setColor(color);
		}

		@Override
		public void setCurrent() {
			if (source != null) source.setCurrent();
		}

		@Override
		public boolean isCurrent() {
			if (source == null) return false;
			return source.isCurrent();
		}

		@Override
		public void setActive(boolean isActive) {
			if (source != null) source.setActive(isActive);
		}

		@Override
		protected boolean isPlaceHolderSource() {
			return false;
		}
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

	public void addShortcuts(
		Collection<? extends AbstractNamedAction> shortcuts)
	{
		shortcuts.forEach(actionsAndBehaviours::addAction);
	}

	private void requestRepaint() {
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public void close() {
		bdvHandle.close();
	}

	public void autoContrast() {
		BdvAutoContrast.autoContrast(imageSource);
	}

}
