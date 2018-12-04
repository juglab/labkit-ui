
package net.imglib2.labkit.brush;

import bdv.util.Affine3DHelpers;
import bdv.viewer.ViewerPanel;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.ActionsAndBehaviours;
import net.imglib2.labkit.brush.neighborhood.TransformedSphere;
import net.imglib2.labkit.models.BitmapModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.logic.BitType;
import net.imglib2.ui.TransformEventHandler;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
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

	final private ViewerPanel viewer;

	private final BitmapModel model;

	final private BrushOverlay brushOverlay;

	private MoveBrush moveBrushBehaviour = new MoveBrush();
	private final PaintBehavior paintBehavior = new PaintBehavior(true);
	private final PaintBehavior eraseBehavior = new PaintBehavior(false);
	private double brushRadius = 1;

	boolean sliceTime;

	final ActionsAndBehaviours behaviors;

	public LabelBrushController(final ViewerPanel viewer, final BitmapModel model,
		final ActionsAndBehaviours behaviors, final boolean sliceTime)
	{
		this.viewer = viewer;
		this.brushOverlay = new BrushOverlay(viewer, model);
		this.sliceTime = sliceTime;
		this.model = model;
		this.behaviors = behaviors;
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

	private class PaintBehavior implements DragBehaviour {

		private boolean value;

		private RealPoint before;

		public PaintBehavior(boolean value) {
			this.value = value;
		}

		private void paint(RealLocalizable coords) {
			synchronized (viewer) {
				final RandomAccessible<BitType> extended = Views.extendValue(bitmap(),
					new BitType(false));
				double brushWidth = getTransformedBrushRadius() * getScale(
					viewerTransformation());
				double brushDepth = brushWidth;
				AffineTransform3D D = brushMatrix(coords, brushWidth, brushDepth);
				AffineTransform3D m = displayToImageTransformation();
				m.concatenate(D);
				Neighborhood<BitType> neighborhood = TransformedSphere.asNeighborhood(
					new long[3], m, extended.randomAccess());
				neighborhood.forEach(pixel -> pixel.set(value));
			}

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
			m.concatenate(model.transformation().inverse());
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
			model.makeVisible();
			RealPoint coords = new RealPoint(x, y);
			this.before = coords;
			paint(coords);
			fireBitmapChanged();
		}

		@Override
		public void drag(final int x, final int y) {
			RealPoint coords = new RealPoint(x, y);
			paint(before, coords);
			this.before = coords;
			brushOverlay.setPosition(x, y);
			fireBitmapChanged();
		}

		@Override
		public void end(final int x, final int y) {}
	}

	private double getTransformedBrushRadius() {
		return brushRadius * getScale(model.transformation());
	}

	// TODO: find a good place
	private double getScale(AffineTransform3D transformation) {
		return IntStream.range(0, 3).mapToDouble(i -> Affine3DHelpers.extractScale(
			transformation, i)).reduce(0, Math::max);
	}

	private RandomAccessibleInterval<BitType> bitmap() {
		if (!model.isValid()) return ArrayImgs.bits(1, 1, 1);
		RandomAccessibleInterval<BitType> label = model.bitmap();
		if (sliceTime) return Views.hyperSlice(label, label.numDimensions() - 1,
			viewer.getState().getCurrentTimepoint());
		return label;
	}

	private void fireBitmapChanged() {
		model.fireBitmapChanged();
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
