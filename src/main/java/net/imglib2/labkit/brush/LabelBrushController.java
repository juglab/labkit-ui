
package net.imglib2.labkit.brush;

import bdv.TransformEventHandler;
import bdv.util.Affine3DHelpers;
import bdv.viewer.ViewerPanel;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.brush.neighborhood.TransformedSphere;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * A {@link TransformEventHandler} that changes an {@link AffineTransform3D}
 * through a set of {@link Behaviour}s.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Philipp Hanslovsky
 */
public class LabelBrushController {

	private final ViewerPanel viewer;

	private final LabelingModel model;

	private final BrushOverlay brushOverlay;

	private final MoveBrush moveBrushBehaviour = new MoveBrush();

	private final PaintBehavior paintBehavior = new PaintBehavior(true);

	private final PaintBehavior eraseBehavior = new PaintBehavior(false);

	private double brushRadius = 1;

	private boolean override = false;

	public LabelBrushController(final ViewerPanel viewer,
		final LabelingModel model, final ActionsAndBehaviours behaviors)
	{
		this.viewer = viewer;
		this.brushOverlay = new BrushOverlay(viewer, model);
		this.model = model;
		brushOverlay.setRadius((int) getTransformedBrushRadius());
		viewer.getDisplay().addOverlayRenderer(brushOverlay);
		installDefaultBehaviors(behaviors);
	}

	private void installDefaultBehaviors(ActionsAndBehaviours behaviors) {
		behaviors.addBehaviour(paintBehaviour(), "paint", "D button1",
			"SPACE button1");
		RunnableAction nop = new RunnableAction("nop", () -> {});
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour(eraseBehaviour(), "erase", "E button1",
			"SPACE button2", "SPACE button3");
		behaviors.addBehaviour(new ChangeBrushRadius(), "change brush radius",
			"D scroll", "E scroll", "SPACE scroll");
		behaviors.addBehaviour(drawBrushBehaviour(), "move brush", "E", "D",
			"SPACE");
	}

	public DragBehaviour drawBrushBehaviour() {
		return moveBrushBehaviour;
	}

	public DragBehaviour paintBehaviour() {
		return paintBehavior;
	}

	public DragBehaviour eraseBehaviour() {
		return eraseBehavior;
	}

	public void setBrushRadius(double brushRadius) {
		this.brushRadius = brushRadius;
		brushOverlay.setRadius((int) getTransformedBrushRadius());
		brushOverlay.requestRepaint();
	}

	public double getBrushRadius() {
		return brushRadius;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	private class PaintBehavior implements DragBehaviour {

		private boolean value;

		private RealPoint before;

		public PaintBehavior(boolean value) {
			this.value = value;
		}

		private void paint(RealLocalizable coords) {
			synchronized (viewer) {
				final RandomAccessible<LabelingType<Label>> extended =
					extendLabelingType(slice());
				double brushWidth = brushSizeInScreenPixel();
				double brushDepth = brushWidth;
				AffineTransform3D D = brushMatrix(coords, brushWidth, brushDepth);
				AffineTransform3D m = displayToImageTransformation();
				m.concatenate(D);
				Neighborhood<LabelingType<Label>> neighborhood = TransformedSphere
					.asNeighborhood(new long[3], m, extended.randomAccess());
				neighborhood.forEach(pixelOperation());
			}

		}

		private double brushSizeInScreenPixel() {
			return getTransformedBrushRadius() * getScale(viewerTransformation());
		}

		private Consumer<LabelingType<Label>> pixelOperation() {
			Label label = model.selectedLabel().get();
			if (value && label != null) {
				if (override) return pixel -> {
					pixel.clear();
					pixel.add(label);
				};
				return pixel -> pixel.add(label);
			}
			else {
				if (override || label == null) return pixel -> pixel.clear();
				return pixel -> pixel.remove(label);
			}
		}

		private RandomAccessible<LabelingType<Label>> extendLabelingType(
			RandomAccessibleInterval<LabelingType<Label>> slice)
		{
			LabelingType<Label> variable = Util.getTypeFromInterval(slice)
				.createVariable();
			variable.clear();
			return Views.extendValue(slice, variable);
		}

		private AffineTransform3D brushMatrix(RealLocalizable coords,
			double brushWidth, double brushDepth)
		{
			AffineTransform3D D = new AffineTransform3D();
			D.set(brushWidth, 0.0, 0.0, coords.getDoublePosition(0), 0.0, brushWidth,
				0.0, coords.getDoublePosition(1), 0.0, 0.0, brushDepth, 0.0);
			return D;
		}

		private AffineTransform3D displayToImageTransformation() {
			AffineTransform3D m = new AffineTransform3D();
			m.concatenate(model.labelTransformation().inverse());
			m.concatenate(viewerTransformation().inverse());
			return m;
		}

		private AffineTransform3D viewerTransformation() {
			AffineTransform3D t = new AffineTransform3D();
			viewer.getState().getViewerTransform(t);
			return t;
		}

		private void paint(RealLocalizable a, RealLocalizable b) {
			long distance = (long) distance(a, b) + 1;
			double step = Math.max(brushRadius * 0.5, 1.0);
			for (long i = 0; i < distance; i += step)
				paint(interpolate((double) i / (double) distance, a, b));
		}

		RealLocalizable interpolate(double ratio, RealLocalizable a,
			RealLocalizable b)
		{
			RealPoint result = new RealPoint(a.numDimensions());
			for (int d = 0; d < result.numDimensions(); d++)
				result.setPosition(ratio * a.getDoublePosition(d) + (1 - ratio) * b
					.getDoublePosition(d), d);
			return result;
		}

		double distance(RealLocalizable a, RealLocalizable b) {
			return LinAlgHelpers.distance(asArray(a), asArray(b));
		}

		private double[] asArray(RealLocalizable a) {
			double[] result = new double[a.numDimensions()];
			a.localize(result);
			return result;
		}

		@Override
		public void init(final int x, final int y) {
			makeLabelVisible();
			RealPoint coords = new RealPoint(x, y);
			this.before = coords;
			paint(coords);
			brushOverlay.setPosition(x, y);
			brushOverlay.setFontVisible(false);
			fireBitmapChanged(coords, coords, brushSizeInScreenPixel());
		}

		@Override
		public void drag(final int x, final int y) {
			RealPoint coords = new RealPoint(x, y);
			paint(before, coords);
			fireBitmapChanged(before, coords, brushSizeInScreenPixel());
			this.before = coords;
			brushOverlay.setPosition(x, y);
		}

		@Override
		public void end(final int x, final int y) {
			brushOverlay.setFontVisible(true);
		}
	}

	private void makeLabelVisible() {
		Label label = model.selectedLabel().get();
		if (label == null) return;
		if (label.isVisible() && model.labelingVisibility().get()) return;
		label.setVisible(true);
		model.labelingVisibility().set(true);
		model.labeling().notifier().notifyListeners();
	}

	private double getTransformedBrushRadius() {
		return brushRadius * getScale(model.labelTransformation());
	}

	// TODO: find a good place
	private double getScale(AffineTransform3D transformation) {
		return IntStream.range(0, 3).mapToDouble(i -> Affine3DHelpers.extractScale(
			transformation, i)).reduce(0, Math::max);
	}

	private RandomAccessibleInterval<LabelingType<Label>> slice() {
		RandomAccessibleInterval<LabelingType<Label>> slice = model.labeling()
			.get();
		if (this.model.isTimeSeries()) return Views.hyperSlice(slice, slice
			.numDimensions() - 1, viewer.getState().getCurrentTimepoint());
		return slice;
	}

	private void fireBitmapChanged(RealPoint a, RealPoint b, double radius) {
		long[] min = new long[2];
		long[] max = new long[2];
		for (int d = 0; d < 2; d++) {
			min[d] = (long) (Math.min(a.getDoublePosition(d), b.getDoublePosition(
				d)) - radius);
			max[d] = (long) (Math.ceil(Math.max(a.getDoublePosition(d), b
				.getDoublePosition(d))) + radius);
		}
		model.dataChangedNotifier().notifyListeners(new FinalInterval(min, max));
	}

	private class ChangeBrushRadius implements ScrollBehaviour {

		@Override
		public void scroll(final double wheelRotation, final boolean isHorizontal,
			final int x, final int y)
		{
			if (!isHorizontal) {
				int sign = (wheelRotation < 0) ? 1 : -1;
				double distance = Math.max(1, brushRadius * 0.1);
				setBrushRadius(Math.min(Math.max(0.5, brushRadius + sign * distance),
					50));

			}
		}
	}

	private class MoveBrush implements DragBehaviour {

		@Override
		public void init(final int x, final int y) {
			brushOverlay.setPosition(x, y);
			brushOverlay.setVisible(true);
			viewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			brushOverlay.requestRepaint();
		}

		@Override
		public void drag(final int x, final int y) {
			brushOverlay.setPosition(x, y);
		}

		@Override
		public void end(final int x, final int y) {
			brushOverlay.setVisible(false);
			viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			brushOverlay.requestRepaint();
		}
	}
}
