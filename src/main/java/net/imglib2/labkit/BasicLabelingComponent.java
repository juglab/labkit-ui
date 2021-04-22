
package net.imglib2.labkit;

import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.DisplayMode;
import net.imglib2.labkit.bdv.BdvAutoContrast;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.bdv.BdvLayerLink;
import net.imglib2.labkit.brush.ChangeLabel;
import net.imglib2.labkit.brush.FloodFillController;
import net.imglib2.labkit.brush.LabelBrushController;
import net.imglib2.labkit.brush.SelectLabelController;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.LabelToolsPanel;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.util.Collection;

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
		setLayout(new MigLayout("", "[grow]", "[][grow]"));
		add(toolsPanel, "wrap, growx");
		add(bdvHandle.getSplitPanel(), "grow");
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

	@Override
	public void close() {
		bdvHandle.close();
	}

	public void autoContrast() {
		BdvAutoContrast.autoContrast(imageSource.get());
	}

}
