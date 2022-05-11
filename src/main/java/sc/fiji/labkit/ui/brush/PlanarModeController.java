
package sc.fiji.labkit.ui.brush;

import bdv.util.BdvHandle;
import bdv.viewer.ViewerStateChange;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import sc.fiji.labkit.ui.models.ImageLabelingModel;
import sc.fiji.labkit.ui.utils.BdvUtils;

import javax.swing.*;

public class PlanarModeController {

	private final BdvHandle bdvHandle;

	private final ImageLabelingModel model;

	private final JSlider zSlider;

	private boolean ignoreChange = false;

	public PlanarModeController(BdvHandle bdvHandle, ImageLabelingModel model,
		JSlider zSlider)
	{
		this.bdvHandle = bdvHandle;
		this.model = model;
		this.zSlider = zSlider;
		zSlider.setVisible(false);
		initZSlider();
	}

	public void setActive(boolean active) {
		if (active) {
			BdvUtils.blockRotateBehaviours(bdvHandle);
			BdvUtils.resetView(bdvHandle.getViewerPanel());
		}
		else {
			BdvUtils.unblockRotateBehaviours(bdvHandle);
		}
		zSlider.setVisible(active);
	}

	private void initZSlider() {
		updateZSliderPosition();
		zSlider.addChangeListener(ignore -> {
			if (ignoreChange)
				return;
			updateViewerTransform();
		});
		bdvHandle.getViewerPanel().state().changeListeners().add(change -> {
			if (change != ViewerStateChange.VIEWER_TRANSFORM_CHANGED)
				return;
			updateZSliderPosition();
		});
	}

	private void updateViewerTransform() {
		Interval interval = getLabelInterval();
		if (interval.numDimensions() < 3)
			return;
		int z = zSlider.getValue();
		double xcenter = 0.5 * (interval.min(0) + interval.max(0));
		double ycenter = 0.5 * (interval.min(1) + interval.max(1));
		double[] v = { xcenter, ycenter, z };
		AffineTransform3D labelTransform = getLabelTransform();
		labelTransform.apply(v, v);
		AffineTransform3D viewerTransform = bdvHandle.getViewerPanel().state().getViewerTransform();
		viewerTransform.apply(v, v);
		viewerTransform.translate(0, 0, -v[2]);
		bdvHandle.getViewerPanel().state().setViewerTransform(viewerTransform);
	}

	private void updateZSliderPosition() {
		Interval interval = getLabelInterval();
		if (interval.numDimensions() < 3)
			return;
		double xcenter = 0.5 * (interval.min(0) + interval.max(0));
		double ycenter = 0.5 * (interval.min(1) + interval.max(1));
		long min = interval.min(2);
		long max = interval.max(2);
		double[] a = { xcenter, ycenter, min };
		double[] b = { xcenter, ycenter, max };
		AffineTransform3D labelTransform = getLabelTransform();
		AffineTransform3D viewerTransform = bdvHandle.getViewerPanel().state().getViewerTransform();
		labelTransform.apply(a, a);
		viewerTransform.apply(a, a);
		labelTransform.apply(b, b);
		viewerTransform.apply(b, b);
		ignoreChange = true;
		zSlider.setMinimum((int) min);
		zSlider.setMaximum((int) max);
		double z = (min * b[2] - max * a[2]) / (b[2] - a[2]);
		if (!Double.isNaN(z))
			zSlider.setValue((int) z);
		ignoreChange = false;
	}

	private Interval getLabelInterval() {
		Interval interval = model.labeling().get().interval();
		if (model.isTimeSeries())
			interval = Intervals.hyperSlice(interval, interval.numDimensions() - 1);
		return interval;
	}

	private AffineTransform3D getLabelTransform() {
		return model.labelTransformation();
	}
}
