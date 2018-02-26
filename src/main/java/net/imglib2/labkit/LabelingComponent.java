package net.imglib2.labkit;

import bdv.util.*;
import bdv.viewer.DisplayMode;
import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.actions.ToggleVisibility;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.control.brush.*;
import net.imglib2.labkit.bdv.BdvLayer;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.panel.HelpPanel;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.util.Pair;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.*;

public class LabelingComponent implements AutoCloseable {

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

	public LabelingComponent( final JFrame dialogBoxOwner,
			final ImageLabelingModel model )
	{
		this.model = model;
		this.dialogBoxOwner = dialogBoxOwner;

		initBdv( model.spatialDimensions().numDimensions() < 3);
		initPanel();
		actionsAndBehaviours = new ActionsAndBehaviours(bdvHandle);
		initLabelsLayer();
		initImageLayer();
		initBrushLayer();
		this.model.transformationModel().initialize( bdvHandle.getViewerPanel() );
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
		panel.add(bdvHandle.getViewerPanel());
	}

	private void initImageLayer() {
		addBdvLayer( new BdvLayer.FinalLayer( model.showable(), "Image", scaledTransformation() ) );
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
		BdvSource source = show( layer, options );
		layer.listeners().add( this::requestRepaint );
		addAction(new ToggleVisibility( layer.title(), source ));
		return source;
	}

	private BdvSource show( BdvLayer layer, BdvOptions options )
	{
		BdvShowable showable = layer.image();
		if( showable.isSpimData() )
			return BdvFunctions.show( showable.spimData(), options ).get(0);
		else
		{
			Pair< Double, Double > minMax = showable.minMax();
			BdvSource source = BdvFunctions.show( RevampUtils.uncheckedCast( showable.image() ), layer.title(), options );
			source.setDisplayRange( minMax.getA(), minMax.getB() );
			return source;
		}
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

	@Override
	public void close()
	{
		bdvHandle.close();
	}
}
