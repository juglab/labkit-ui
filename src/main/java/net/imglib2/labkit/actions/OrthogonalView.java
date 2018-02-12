package net.imglib2.labkit.actions;

import net.imglib2.Dimensions;
import net.imglib2.labkit.Extensible;
import net.imglib2.labkit.models.TransformationModel;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * @author Matthias Arzt
 */
public class OrthogonalView {

	public OrthogonalView( Extensible extensible, ImageLabelingModel model ) {
		extensible.addAction("Orthogonal View", "resetView", () -> {
			TransformationModel transformationModel = model.transformationModel();
			transformationModel.transformToShowInterval( model.image() );
		}, "");
	}
}
