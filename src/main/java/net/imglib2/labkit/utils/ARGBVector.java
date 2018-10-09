
package net.imglib2.labkit.utils;

import net.imglib2.type.numeric.ARGBType;

public class ARGBVector {

	private int red = 0, green = 0, blue = 0, alpha = 0;

	public void add(ARGBType color) {
		int colorCode = color.get();
		red += ARGBType.red(colorCode);
		green += ARGBType.green(colorCode);
		blue += ARGBType.blue(colorCode);
		alpha += ARGBType.alpha(colorCode);
	}

	public void div(int value) {
		red /= value;
		green /= value;
		blue /= value;
		alpha /= value;
	}

	public ARGBType get() {
		return new ARGBType(getAsint());
	}

	public int getAsint() {
		int r = upperBound(red);
		int g = upperBound(green);
		int b = upperBound(blue);
		int a = upperBound(alpha);
		return ARGBType.rgba(r, g, b, a);
	}

	private int upperBound(int value) {
		return Math.min(255, value);
	}

}
