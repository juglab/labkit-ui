package net.imglib2.labkit.inputimage;

import net.imglib2.realtransform.AffineTransform3D;

public abstract class AbstractInputImage implements InputImage{

	private AffineTransform3D transformation = new AffineTransform3D();

	@Override
	public void setTransformation(AffineTransform3D transformation ) {
		this.transformation = transformation;
	}

	@Override
	public AffineTransform3D transformation() {
		return transformation;
	}
}
