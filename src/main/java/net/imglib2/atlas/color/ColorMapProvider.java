package net.imglib2.atlas.color;

import java.awt.Color;
import java.util.*;
import java.util.function.Consumer;

import net.imglib2.atlas.Holder;
import net.imglib2.atlas.Notifier;
import net.imglib2.atlas.labeling.Labeling;
import net.imglib2.type.numeric.ARGBType;

public class ColorMapProvider
{

	private final Random rng = new Random(42);

	private ColorMap colorMap;

	private final Notifier<Consumer<ColorMap>> listeners = new Notifier<>();

	public ColorMapProvider(Holder<Labeling> labelingHolder)
	{
		updateLabeling(labelingHolder.get());
		labelingHolder.notifier().add(this::updateLabeling);
	}

	public ColorMap colorMap() {
		return colorMap;
	}

	public Notifier<Consumer<ColorMap>> listeners() {
		return listeners;
	}

	private void updateLabeling(Labeling labeling) {
		updateColors(new ArrayList<>(labeling.regions().keySet()));
		listeners.forEach(x -> x.accept(colorMap));
	}

	private void updateColors(final List<String> keys)
	{
		Map<String, ARGBType> map = new TreeMap<>();
		final float step = 1.0f / keys.size();
		final float start = rng.nextFloat();
		for (int i = 0; i < keys.size(); i++) {
			float x = start + step * i;
			float y = x > 1.0f ? x - 1.0f : x;
			map.put(keys.get(i), new ARGBType(Color.HSBtoRGB(y, 1.0f, 1.0f)));
		}
		colorMap = map::get;
	}
}
