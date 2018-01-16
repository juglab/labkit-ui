package net.imglib2.labkit;

import bdv.util.*;
import bdv.viewer.DisplayMode;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.actions.ToggleVisibility;
import net.imglib2.labkit.control.brush.*;
import net.imglib2.labkit.labeling.LabelsLayer;
import net.imglib2.labkit.panel.HelpPanel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.ui.OverlayRenderer;
import net.imglib2.util.Pair;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import javax.swing.*;
import java.awt.*;

public class LabelingComponent {

	private BdvHandle bdvHandle;

	private JPanel panel = new JPanel();

	private final JFrame dialogBoxOwner;

	private ActionsAndBehaviours actionsAndBehaviours;

	private AffineTransform3D sourceTransformation = new AffineTransform3D();

	private LabelingModel model;

	public JComponent getComponent() {
		return panel;
	}

	public ActionMap getActions() {
		return actionsAndBehaviours.getActions();
	}

	LabelingComponent(final JFrame dialogBoxOwner,
			final ImageLabelingModel model,
			final boolean isTimeSeries)
	{
		this.model = model;
		this.dialogBoxOwner = dialogBoxOwner;

		final int nDim = model.image().numDimensions() - (isTimeSeries ? 1 : 0);

		initBdv(nDim  < 3);
		panel.add(new HelpPanel(), BorderLayout.PAGE_START);

		actionsAndBehaviours = new ActionsAndBehaviours(bdvHandle);

		initLabelsLayer(isTimeSeries);

		Pair<Double, Double> p = AtlasUtils.estimateMinMax(model.image());
		BdvStackSource<?> source = addLayer(RevampUtils.uncheckedCast(model.image()), "original");
		source.setDisplayRange(p.getA(), p.getB());
		addAction(new ToggleVisibility("Image", source));
	}

	public void addAction(AbstractNamedAction action) {
		actionsAndBehaviours.addAction(action);
	}

	public void addBehaviour(Behaviour behaviour, String name, String defaultTriggers) {
		actionsAndBehaviours.addBehaviour(behaviour, name, defaultTriggers);
	}

	private void initBdv(boolean is2D) {
		final BdvOptions options = BdvOptions.options();
		if (is2D)
			options.is2D();
		bdvHandle = new BdvHandlePanel(dialogBoxOwner, options);
		panel.setLayout(new BorderLayout());
		panel.add(bdvHandle.getViewerPanel());
		bdvHandle.getViewerPanel().setDisplayMode( DisplayMode.FUSED );
	}

	private void initLabelsLayer(boolean isTimeSeries) {
		model.dataChangedNotifier().add(this::requestRepaint);
		BdvSource source = addLayer(new LabelsLayer(model).view(), "labels");
		addAction(new ToggleVisibility( "Labeling", source ));
		final LabelBrushController brushController = new LabelBrushController(
				bdvHandle.getViewerPanel(),
				model,
				actionsAndBehaviours,
				sourceTransformation,
				isTimeSeries);
		bdvHandle.getViewerPanel().getDisplay().addOverlayRenderer( brushController.getBrushOverlay() );
	}

	private void requestRepaint() {
		bdvHandle.getViewerPanel().requestRepaint();
	}

	public <T extends NumericType<T>> BdvStackSource<T> addLayer(RandomAccessibleInterval<T> interval, String prediction) {
		return BdvFunctions.show(interval, prediction, BdvOptions.options().addTo(bdvHandle).sourceTransform(sourceTransformation));
	}

	public AffineTransform3D sourceTransformation() {
		return sourceTransformation;
	}

	public Object viewerSync() {
		return bdvHandle.getViewerPanel();
	}
}
