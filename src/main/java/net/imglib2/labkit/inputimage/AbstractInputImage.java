package net.imglib2.labkit.inputimage;

public abstract class AbstractInputImage implements InputImage{

	private double scaling = 1.0;

	@Override
	public void setScaling(double scaling) {
		this.scaling = scaling;
	}

	@Override
	public double scaling() {
		return scaling;
	}
}
