package net.imglib2.labkit.actions;

import bdv.viewer.ViewerPanel;
import net.imglib2.labkit.Extensible;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * @author Matthias Arzt
 */
public class OrthogonalView {

	public OrthogonalView(Extensible extensible, AffineTransform3D transformation) {
		extensible.addAction("Orthogonal View", "resetView", () -> {
			ViewerPanel p = (ViewerPanel) extensible.viewerSync();
			p.setCurrentViewerTransform(transformation);
		}, "");
	}
}
