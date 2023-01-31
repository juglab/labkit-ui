/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
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
import bdv.util.BdvHandle;
import bdv.viewer.ViewerPanel;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.type.logic.BitType;
import org.scijava.ui.behaviour.*;
import sc.fiji.labkit.ui.ActionsAndBehaviours;
import sc.fiji.labkit.ui.brush.neighborhood.Ellipsoid;
import sc.fiji.labkit.ui.brush.neighborhood.RealPoints;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.LabelingModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.LinAlgHelpers;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.util.RunnableAction;
import sc.fiji.labkit.ui.panel.GuiUtils;
import sc.fiji.labkit.ui.utils.Notifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.Arrays;
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

	private final BdvHandle bdv;

	private final ViewerPanel viewer;

	private final LabelingModel model;

	private final BrushCursor brushCursor;

	private final MoveBrush moveBrushBehaviour = new MoveBrush();

	private final MouseAdapter moveBrushAdapter = GuiUtils.toMouseListener(moveBrushBehaviour);

	private final PaintBehavior paintBehaviour = new PaintBehavior(true);

	private final PaintBehavior eraseBehaviour = new PaintBehavior(false);

	private double brushDiameter = 1;

	private final Notifier brushDiameterListeners = new Notifier();

	private boolean overlapping = false;

	private boolean keepBrushCursorVisible = false;

	private boolean planarMode = false;

	public LabelBrushController(final BdvHandle bdv,
		final LabelingModel model, final ActionsAndBehaviours behaviors)
	{
		this.bdv = bdv;
		this.viewer = bdv.getViewerPanel();
		this.brushCursor = new BrushCursor(model);
		this.model = model;
		updateBrushOverlayRadius();
		viewer.getDisplay().addOverlayRenderer(brushCursor);
		viewer.addTransformListener(affineTransform3D -> updateBrushOverlayRadius());
		installDefaultBehaviors(behaviors);
	}

	private void installDefaultBehaviors(ActionsAndBehaviours behaviors) {
		behaviors.addBehaviour(paintBehaviour, "paint", "D button1",
			"SPACE button1");
		RunnableAction nop = new RunnableAction("nop", () -> {});
		nop.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F"));
		behaviors.addAction(nop);
		behaviors.addBehaviour(eraseBehaviour, "erase", "E button1",
			"SPACE button2", "SPACE button3");
		behaviors.addBehaviour(new ChangeBrushRadius(), "change brush radius",
			"D scroll", "E scroll", "SPACE scroll");
		behaviors.addBehaviour(moveBrushBehaviour, "move brush", "E", "D",
			"SPACE");
	}

	public void setBrushActive(boolean active) {
		BdvMouseBehaviourUtils.setMouseBehaviourActive(bdv, paintBehaviour, active);
		setBrushCursorActive(active);
		keepBrushCursorVisible = active;
	}

	public void setEraserActive(boolean active) {
		BdvMouseBehaviourUtils.setMouseBehaviourActive(bdv, eraseBehaviour, active);
		setBrushCursorActive(active);
		keepBrushCursorVisible = active;
	}

	private void setBrushCursorActive(boolean visible) {
		if (visible) {
			viewer.getDisplay().addMouseListener(moveBrushAdapter);
			viewer.getDisplay().addMouseMotionListener(moveBrushAdapter);
		}
		else {
			viewer.getDisplay().removeMouseListener(moveBrushAdapter);
			viewer.getDisplay().removeMouseMotionListener(moveBrushAdapter);
		}
	}

	public void setBrushDiameter(double brushDiameter) {
		this.brushDiameter = brushDiameter;
		updateBrushOverlayRadius();
		triggerBrushOverlayRepaint();
		brushDiameterListeners.notifyListeners();
	}

	private void updateBrushOverlayRadius() {
		brushCursor.setRadius(getBrushDisplayRadius());
	}

	private void triggerBrushOverlayRepaint() {
		viewer.getDisplay().repaint();
	}

	public double getBrushDiameter() {
		return brushDiameter;
	}

	public Notifier brushDiameterListeners() {
		return brushDiameterListeners;
	}

	public void setOverlapping(boolean overlapping) {
		this.overlapping = overlapping;
	}

	public void setPlanarMode(boolean planarMode) {
		this.planarMode = planarMode;
	}

	private class PaintBehavior implements DragBehaviour {

		private boolean value;

		private RealPoint before;

		public PaintBehavior(boolean value) {
			this.value = value;
		}

		private void paint(RealLocalizable screenCoordinates) {
			synchronized (viewer) {
				RandomAccessible<LabelingType<Label>> extended = extendLabelingType(getFrame());
				double radius = Math.max(0, (brushDiameter - 1) * 0.5);
				AffineTransform3D m = displayToImageTransformation();
				double[] screen = { screenCoordinates.getDoublePosition(0), screenCoordinates
					.getDoublePosition(1), 0 };
				double[] center = new double[3];
				m.apply(screen, center);
				if (extended.numDimensions() == 3 && planarMode)
					extended = Views.hyperSlice(extended, 2, Math.round(center[2]));
				AffineTransform3D labelTransform = model.labelTransformation();
				double pixelWidth = RealPoints.length(labelTransform.d(0));
				double pixelHeight = RealPoints.length(labelTransform.d(1));
				double pixelDepth = RealPoints.length(labelTransform.d(2));
				double[] axes = { radius, radius * pixelWidth / pixelHeight, radius * pixelWidth /
					pixelDepth };
				if (extended.numDimensions() == 2) {
					center = Arrays.copyOf(center, 2);
					axes = Arrays.copyOf(axes, 2);
				}
				IterableRegion<BitType> region = Ellipsoid.asIterableRegion(center, axes);
				Regions.sample(region, extended).forEach(pixelOperation());
			}

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
			long distance = (long) (4 * (distance(a, b) + 1));
			long step = (long) Math.max(brushDiameter, 1.0);
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
			brushCursor.setPosition(x, y);
			brushCursor.setFontVisible(false);
			makeLabelVisible();
			RealPoint coords = new RealPoint(x, y);
			this.before = coords;
			paint(coords);
			double radius = getBrushDisplayRadius();
			fireBitmapChanged(coords, coords, radius);
		}

		@Override
		public void drag(final int x, final int y) {
			brushCursor.setPosition(x, y);
			RealPoint coords = new RealPoint(x, y);
			paint(before, coords);
			double radius = getBrushDisplayRadius();
			fireBitmapChanged(before, coords, radius);
			this.before = coords;
		}

		@Override
		public void end(final int x, final int y) {
			brushCursor.setPosition(x, y);
			brushCursor.setFontVisible(true);
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

	private double getBrushDisplayRadius() {
		return brushDiameter * 0.5 * getScale(model.labelTransformation()) *
			getScale(paintBehaviour.viewerTransformation());
	}

	// TODO: find a good place
	private double getScale(AffineTransform3D transformation) {
		return Affine3DHelpers.extractScale(transformation, 0);
	}

	private RandomAccessibleInterval<LabelingType<Label>> getFrame() {
		RandomAccessibleInterval<LabelingType<Label>> frame = model.labeling()
			.get();
		if (this.model.isTimeSeries()) return Views.hyperSlice(frame, frame
			.numDimensions() - 1, viewer.state().getCurrentTimepoint());
		return frame;
	}

	private void fireBitmapChanged(RealPoint a, RealPoint b, double radius) {
		radius = radius * (brushDiameter + 2) / brushDiameter;
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
				double distance = Math.max(1, brushDiameter * 0.1);
				setBrushDiameter(Math.min(Math.max(1, brushDiameter + sign * distance), 50));
			}
		}
	}

	private class MoveBrush implements DragBehaviour {

		@Override
		public void init(final int x, final int y) {
			brushCursor.setPosition(x, y);
			brushCursor.setVisible(true);
			viewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			triggerBrushOverlayRepaint();
		}

		@Override
		public void drag(final int x, final int y) {
			brushCursor.setPosition(x, y);
		}

		@Override
		public void end(final int x, final int y) {
			brushCursor.setPosition(x, y);
			if (!keepBrushCursorVisible) {
				brushCursor.setVisible(false);
				viewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			triggerBrushOverlayRepaint();
		}
	}
}
