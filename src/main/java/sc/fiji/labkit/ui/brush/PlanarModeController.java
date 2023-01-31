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
			BdvUtils.blockRotation(bdvHandle);
			BdvUtils.resetView(bdvHandle.getViewerPanel());
		}
		else {
			BdvUtils.unblockRotation(bdvHandle);
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
