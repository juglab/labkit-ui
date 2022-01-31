/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

package sc.fiji.labkit.ui.brush;

import bdv.util.Affine3DHelpers;
import bdv.viewer.ViewerPanel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.type.logic.BitType;
import sc.fiji.labkit.ui.ActionsAndBehaviours;
import sc.fiji.labkit.ui.brush.neighborhood.TransformedSphere;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.LabelingModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.DragBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class implements the brush and eraser tool.
 *
 * @author Matthias Arzt
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

	private boolean overlapping = false;

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
		brushOverlay.setRadius(getTransformedBrushRadius());
		brushOverlay.requestRepaint();
	}

	public double getBrushRadius() {
		return brushRadius;
	}

	public void setOverlapping(boolean overlapping) {
		this.overlapping = overlapping;
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
				AffineTransform3D D = brushMatrix(coords, brushWidth, brushWidth);
				AffineTransform3D m = displayToImageTransformation();
				m.concatenate(D);
				TransformedSphere sphere = new TransformedSphere(m);
				IterableRegion<BitType> region = TransformedSphere.iterableRegion(sphere, extended
					.numDimensions());
				Regions.sample(region, extended).forEach(pixelOperation());
			}

		}

		private double brushSizeInScreenPixel() {
			return getTransformedBrushRadius() * getScale(viewerTransformation());
		}

		private Consumer<LabelingType<Label>> pixelOperation() {
			Label label = model.selectedLabel().get();
			if (value) {
				if (label != null) {
					if (overlapping) return pixel -> pixel.add(label);
					List<Label> visibleLabels = getVisibleLabels();
					return pixel -> {
						pixel.removeAll(visibleLabels);
						pixel.add(label);
					};
				}
				else
					return pixel -> {};
			}
			else {
				if (overlapping && label != null)
					return pixel -> pixel.remove(label);
				List<Label> visibleLabels = getVisibleLabels();
				return pixel -> pixel.removeAll(visibleLabels);
			}
		}

		private List<Label> getVisibleLabels() {
			List<Label> visibleLabels =
				model.labeling().get().getLabels().stream()
					.filter(Label::isVisible)
					.collect(Collectors.toList());
			return visibleLabels;
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
			viewer.state().getViewerTransform(t);
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
		return Affine3DHelpers.extractScale(transformation, 0);
	}

	private RandomAccessibleInterval<LabelingType<Label>> slice() {
		RandomAccessibleInterval<LabelingType<Label>> slice = model.labeling()
			.get();
		if (this.model.isTimeSeries()) return Views.hyperSlice(slice, slice
			.numDimensions() - 1, viewer.state().getCurrentTimepoint());
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
				setBrushRadius(Math.min(Math.max(1, brushRadius + sign * distance), 50));
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
