
package net.imglib2.labkit;

import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.SynchronizedViewerState;
import bdv.viewer.ViewerStateChange;
import net.imglib2.labkit.bdv.BdvAutoContrast;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.brush.ChangeLabel;
import net.imglib2.labkit.brush.FloodFillController;
import net.imglib2.labkit.brush.LabelBrushController;
import net.imglib2.labkit.brush.SelectLabelController;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.models.DefaultHolder;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.LabelToolsPanel;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.util.Collection;

public class BasicLabelingComponent implements AutoCloseable {

	private final Holder<BdvStackSource<?>> imageSource;

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

	private Holder<BdvStackSource<?>> initImageLayer() {
		return addBdvLayer(new BdvLayer.FinalLayer(model.showable(), "Image", model
			.imageVisibility()));
	}

	private void initLabelsLayer() {
		addBdvLayer(new LabelsLayer(model));
	}

	public Holder<BdvStackSource<?>> addBdvLayer(BdvLayer layer) {
		BdvOptions options = BdvOptions.options().addTo(bdvHandle);
		Holder<BdvShowable> image = layer.image();
		Holder<BdvStackSource<?>> source = new DefaultHolder<>(image.get().show(layer.title(),
			options));
		image.notifier().add(() -> {
			source.get().removeFromBdv();
			source.set(image.get().show(layer.title(), options));
			source.get().setActive(layer.visibility().get());
		});
		layer.listeners().add(this::requestRepaint);
		layer.visibility().notifier().add(() -> {
			try {
				source.get().setActive(layer.visibility().get());
			}
			catch (NullPointerException ignore) {
				// ignore
			}
		});
		SynchronizedViewerState viewerState = bdvHandle.getViewerPanel().state();
		viewerState.changeListeners().add(l -> {
			if (l == ViewerStateChange.VISIBILITY_CHANGED) {
				boolean visible = viewerState.isSourceActive(source.get().getSources().get(0));
				layer.visibility().set(visible);
			}
		});
		return source;
	}

	private JPanel initBrushLayer() {
		final LabelBrushController brushController = new LabelBrushController(
			bdvHandle.getViewerPanel(), model, actionsAndBehaviours);
		final FloodFillController floodFillController = new FloodFillController(
			bdvHandle.getViewerPanel(), model, actionsAndBehaviours);
		final SelectLabelController selectLabelController =
			new SelectLabelController(bdvHandle.getViewerPanel(), model,
				actionsAndBehaviours);
		JPanel toolsPanel = new LabelToolsPanel(bdvHandle, brushController,
			floodFillController, selectLabelController);
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
		BdvAutoContrast.autoContrast(imageSource.get());
	}

}
