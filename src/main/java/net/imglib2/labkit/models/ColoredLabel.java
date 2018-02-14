package net.imglib2.labkit.models;

import net.imglib2.type.numeric.ARGBType;

public class ColoredLabel extends Label{
	
	public ARGBType color;
	
	public ColoredLabel( String label, ARGBType color ) {
		super(label);
		this.color = color;
	}
}
