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
			AffineTransform3D transformation = initialTransformation( transformationModel, model.spatialDimensions() );
			transformationModel.setTransformation( transformation );
		}, "");
	}

	private AffineTransform3D initialTransformation( TransformationModel transformationModel, Dimensions dimensions )
	{
		double imageWidth = dimensions.dimension( 0 );
		double imageHeight = dimensions.dimension( 1 );
		double imageDepth = dimensions.numDimensions() > 2 ? dimensions.dimension( 2 ) : 0;
		double screenWidth = transformationModel.width();
		double screenHeight = transformationModel.height();
		double zoom = Math.min( screenWidth / imageWidth, screenHeight / imageHeight);
		AffineTransform3D transformation = new AffineTransform3D();
		transformation.set(
				zoom, 0, 0, (screenWidth - imageWidth * zoom) * 0.5,
				0, zoom, 0, ( screenHeight - imageHeight * zoom) * 0.5,
				0, 0, zoom, - imageDepth * zoom * 0.5
		);
		return transformation;
	}
}
