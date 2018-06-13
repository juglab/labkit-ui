
package net.imglib2.labkit.models;

import net.imglib2.type.numeric.ARGBType;

public class ColoredLabel {

	public ARGBType color;

	public String name;

	public ColoredLabel(String label, ARGBType color) {
		this.name = label;
		this.color = color;
	}
}
