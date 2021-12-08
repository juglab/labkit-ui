
package sc.fiji.labkit.ui.utils;

import net.imglib2.type.numeric.ARGBType;

import java.awt.*;
import java.util.function.Supplier;

public class ColorSupplier implements Supplier<ARGBType> {

	private static float GOLDEN_RATIO = (float) ((1 + Math.sqrt(5)) / 2);
	private float GOLDEN_ANGLE = 1f - 1f / GOLDEN_RATIO;
	float hue = 1f - 2f * GOLDEN_ANGLE;

	@Override
	public ARGBType get() {
		hue += GOLDEN_ANGLE;
		if (hue > 1f) hue -= 1f;
		return new ARGBType(Color.HSBtoRGB(hue, 1f, 1f));
	}
}
