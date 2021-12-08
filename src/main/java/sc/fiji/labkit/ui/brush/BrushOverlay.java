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
/**
 *
 */

package sc.fiji.labkit.ui.brush;

import bdv.util.Affine3DHelpers;
import bdv.viewer.OverlayRenderer;
import bdv.viewer.ViewerPanel;
import sc.fiji.labkit.ui.labeling.Label;
import sc.fiji.labkit.ui.models.LabelingModel;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;

/**
 * Overlay for Big Data Viewer that shows a circle, and a label title at a given
 * location.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 */
public class BrushOverlay implements OverlayRenderer {

	private final ViewerPanel viewer;
	private final LabelingModel model;
	private int x, y;
	private double radius = 5;
	private boolean visible = false;
	private boolean fontVisible = true;
	final AffineTransform3D viewerTransform = new AffineTransform3D();

	public BrushOverlay(ViewerPanel viewer, LabelingModel model) {
		this.viewer = viewer;
		this.model = model;
	}

	public void setPosition(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public void setFontVisible(final boolean visible) {
		this.fontVisible = visible;
	}

	public void requestRepaint() {
		viewer.getDisplay().repaint();
	}

	@Override
	public void drawOverlays(final Graphics g) {
		if (visible) {
			final Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setComposite(AlphaComposite.SrcOver);
			final int roundScaledRadius = (int) Math.round(getScale() * radius);
			Label label = model.selectedLabel().get();
			if (label != null) {
				Color color = new Color(label.color().get());
				String title = label.name();
				if (fontVisible) drawTitle(g2d, color, title, roundScaledRadius);
				drawCircle(g2d, color, roundScaledRadius);
			}
		}
	}

	@Override
	public void setCanvasSize(int width, int height) {

	}

	private double getScale() {
		synchronized (viewer) {
			viewer.state().getViewerTransform(viewerTransform);
			return Affine3DHelpers.extractScale(viewerTransform, 0);
		}
	}

	private void drawCircle(Graphics2D g2d, Color color, int roundScaledRadius) {
		g2d.setColor(color);
		g2d.setStroke(new BasicStroke(1));
		g2d.drawOval(x - roundScaledRadius, y - roundScaledRadius, 2 *
			roundScaledRadius + 1, 2 * roundScaledRadius + 1);
	}

	private void drawTitle(Graphics2D g2d, Color color, String title,
		int roundScaledRadius)
	{
		g2d.setColor(color);
		g2d.drawString(title, x + roundScaledRadius + 20, y + roundScaledRadius +
			20);
	}
}
