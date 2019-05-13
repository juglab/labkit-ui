/**
 *
 */

package net.imglib2.labkit.brush;

import bdv.util.Affine3DHelpers;
import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.labeling.Label;
import net.imglib2.labkit.models.LabelingModel;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.OverlayRenderer;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 */
public class BrushOverlay implements OverlayRenderer {

	private final ViewerPanel viewer;

	private final LabelingModel model;

	private int x, y, radius = 5;
	private boolean visible = false;
	final AffineTransform3D viewerTransform = new AffineTransform3D();

	public BrushOverlay(ViewerPanel viewer, LabelingModel model) {
		this.viewer = viewer;
		this.model = model;
	}

	public void setPosition(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public void setRadius(final int radius) {
		this.radius = radius;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
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
				drawTitle(g2d, color, title, roundScaledRadius);
				drawCircle(g2d, color, roundScaledRadius);
			}
		}
	}

	@Override
	public void setCanvasSize(int width, int height) {

	}

	private double getScale() {
		synchronized (viewer) {
			viewer.getState().getViewerTransform(viewerTransform);
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
		final FontMetrics fm = g2d.getFontMetrics();
		final Rectangle2D rect = fm.getStringBounds(title, g2d);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(x + roundScaledRadius, y + roundScaledRadius - fm.getAscent(),
			(int) rect.getWidth(), (int) rect.getHeight());
		g2d.setColor(color);
		g2d.drawString(title, x + roundScaledRadius, y + roundScaledRadius);
	}
}
