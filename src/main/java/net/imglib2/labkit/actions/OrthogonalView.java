package net.imglib2.labkit.actions;

import net.imglib2.labkit.Extensible;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * @author Matthias Arzt
 */
public class OrthogonalView {

	public OrthogonalView(Extensible extensible) {
		extensible.addAction("Orthogonal View", "resetView", () -> {
			extensible.setViewerTransformation(new AffineTransform3D());
		}, "");
	}
}
