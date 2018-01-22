package net.imglib2.labkit.color;

import java.awt.*;
import java.util.*;
import java.util.function.Supplier;

import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.type.numeric.ARGBType;

public class ColorMapProvider
{

	private ColorMap colorMap;

	public ColorMapProvider(Holder<Labeling> labelingHolder)
	{
		colorMap = initColorMap();
	}

	public ColorMap colorMap() {
		return colorMap;
	}

	private static ColorMap initColorMap()
	{
		Map<String, ARGBType> map = new TreeMap<>();
		final ColorSupplier colorSupplier = new ColorSupplier();
		return new ColorMap() {
			@Override
			public ARGBType getColor(String key) {
				return map.computeIfAbsent(key, x -> colorSupplier.get());
			}

			@Override
			public void setColor(String key, ARGBType color) {
				map.put(key, color);
			}
		};
	}

	private static class ColorSupplier implements Supplier<ARGBType> {

		private static float GOLDEN_RATIO = (float) ((1 + Math.sqrt(5)) / 2);
		private float GOLDEN_ANGLE = 1f - 1f / GOLDEN_RATIO;
		float hue = 1f - 2f * GOLDEN_ANGLE;

		@Override
		public ARGBType get() {
			hue += GOLDEN_ANGLE;
			if(hue > 1f) hue -= 1f;
			return new ARGBType(Color.HSBtoRGB(hue, 1f, 1f));
		}
	}
}
