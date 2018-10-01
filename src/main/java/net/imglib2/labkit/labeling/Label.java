
package net.imglib2.labkit.labeling;

import net.imglib2.type.numeric.ARGBType;

public class Label {

	private String name;

	private final ARGBType color;

	public Label(String name, ARGBType color) {
		this.name = name;
		this.color = new ARGBType();
		this.color.set(color);
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ARGBType color() {
		return color;
	}

	public void setColor(ARGBType color) {
		this.color.set(color);
	}
}
