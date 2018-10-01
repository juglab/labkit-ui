
package net.imglib2.labkit.models;

import net.imglib2.labkit.labeling.Label;
import net.imglib2.type.numeric.ARGBType;

public class ColoredLabel {

	public ARGBType color;

	public Label label;

	public ColoredLabel(Label label, ARGBType color) {
		this.label = label;
		this.color = color;
	}
}
