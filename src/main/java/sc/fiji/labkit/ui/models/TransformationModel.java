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

package sc.fiji.labkit.ui.models;

import bdv.viewer.ViewerPanel;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import sc.fiji.labkit.pixel_classification.RevampUtils;
import sc.fiji.labkit.ui.utils.BdvUtils;

/**
 * Needs to be refactored, this is a strange way to enable the "reset view" and
 * "focus label" functionality.
 */
public class TransformationModel {

	private final boolean isTimeSeries;

	private ViewerPanel viewerPanel;

	public TransformationModel(boolean isTimeSeries) {
		this.isTimeSeries = isTimeSeries;
	}

	public void initialize(ViewerPanel viewerPanel) {
		this.viewerPanel = viewerPanel;
	}

	public void transformToShowInterval(Interval interval,
		AffineTransform3D transformation)
	{
		if (viewerPanel == null)
			return;
		if (isTimeSeries) {
			int lastDim = interval.numDimensions() - 1;
			long meanTimePoint = (interval.min(lastDim) + interval.max(lastDim)) / 2;
			if (viewerPanel != null) viewerPanel.setTimepoint((int) meanTimePoint);
			interval = RevampUtils.removeLastDimension(interval);
		}
		BdvUtils.resetView(viewerPanel, interval, transformation);
	}
}
