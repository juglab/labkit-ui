package net.imglib2.labkit;

import java.awt.BorderLayout;

import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.imglib2.labkit.actions.ToggleVisibility;
import net.imglib2.labkit.control.brush.ChangeLabel;
import net.imglib2.labkit.control.brush.LabelBrushController;
import net.imglib2.labkit.labeling.BdvLayer;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.HelpPanel;
import net.imglib2.labkit.panel.LabelActionsPanel;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.util.Pair;

import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;

public class LabelingComponent {

	private BdvHandle bdvHandle;

	private JPanel panel = new JPanel();

	private final JFrame dialogBoxOwner;

	private ActionsAndBehaviours actionsAndBehaviours;

	private ImageLabelingModel model;

	public JComponent getComponent() {
		return panel;
	}

	public ActionMap getActions() {
		return actionsAndBehaviours.getActions();
	}

	LabelingComponent(final JFrame dialogBoxOwner,
			final ImageLabelingModel model)
	{
		this.model = model;
		this.dialogBoxOwner = dialogBoxOwner;

		final int nDim = model.image().numDimensions() - (model.isTimeSeries() ? 1 : 0);

		initBdv(nDim  < 3);
		actionsAndBehaviours = new ActionsAndBehaviours(bdvHandle);
		initPanel();
		initLabelsLayer();
		initImageLayer();
		initBrushLayer();
	}

	private void initBdv(boolean is2D) {
		final BdvOptions options = BdvOptions.options();
		if (is2D)
			options.is2D();
		bdvHandle = new BdvHandlePanel(dialogBoxOwner, options);
		bdvHandle.getViewerPanel().setDisplayMode( DisplayMode.FUSED );
	}

	private void initPanel() {
		panel.setLayout(new BorderLayout());
		panel.add(new HelpPanel(), BorderLayout.PAGE_START);
		//TODO finish LabelActionsPanel
//		panel.add(new LabelActionsPanel(actionsAndBehaviours), BorderLayout.PAGE_START);
		panel.add(bdvHandle.getViewerPanel());
	}

	private void initImageLayer() {
		Pair<Double, Double> minMax = LabkitUtils.estimateMinMax( model.image() );
		addBdvLayer( new BdvLayer.FinalLayer( model.image(), "Image", scaledTransformation() ) )
				.setDisplayRange( minMax.getA(), minMax.getB());
	}

	private AffineTransform3D scaledTransformation() {
		AffineTransform3D transformation = new AffineTransform3D();
		transformation.scale(model.scaling());
		return transformation;
	}

	private void initLabelsLayer() {
		addBdvLayer( new LabelsLayer( model ) );
	}

	public BdvSource addBdvLayer( BdvLayer layer )
	{
		BdvOptions options = BdvOptions.options().addTo( bdvHandle ).sourceTransform( layer.transformation() );
		BdvSource source = BdvFunctions.show( RevampUtils.uncheckedCast( layer.image() ), layer.title(), options );
		layer.listeners().add( this::requestRepaint );
		addAction(new ToggleVisibility( layer.title(), source ));
		return source;
	}

	private void initBrushLayer()
	{
		final LabelBrushController brushController = new LabelBrushController(
				bdvHandle.getViewerPanel(),
				new BitmapModel( model ),
				actionsAndBehaviours,
				model.isTimeSeries());
		actionsAndBehaviours.addAction( new ChangeLabel( model ) );
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
	}

	public void addAction(AbstractNamedAction action) {
		actionsAndBehaviours.addAction(action);
	}

	private void requestRepaint() {
		bdvHandle.getViewerPanel().requestRepaint();
	}

	public ViewerPanel viewerPanel() {
		return bdvHandle.getViewerPanel();
	}
}
