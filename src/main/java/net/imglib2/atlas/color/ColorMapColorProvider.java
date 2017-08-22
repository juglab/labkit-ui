package net.imglib2.atlas.color;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.IntStream;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.type.numeric.ARGBType;

public class ColorMapColorProvider implements IntegerColorProvider
{
	private final Map<String, ARGBType> colors;

	private Random rng = new Random(42);

	public ColorMapColorProvider(final List<String> keys)
	{
		colors = new TreeMap<>();
		setColors(keys);
	}

	public void setColors(final List<String> keys)
	{
		final float step = 1.0f / keys.size();
		final float start = rng.nextFloat();
		final int[] values = IntStream
				.range( 0, keys.size() )
				.mapToDouble( i -> start + step * i )
				.map( d -> d > 1.0 ? d - 1.0 : d )
				.mapToInt( d -> Color.HSBtoRGB( ( float ) d, 1.0f, 1.0f ) ).toArray();
		setColors( keys, values );
	}

	private void setColors( final List<String> keys, final int[] values )
	{
		this.colors.clear();
		for ( int i = 0; i < keys.size(); ++i )
			this.colors.put( keys.get(i), new ARGBType(values[ i ]) );
	}

	@Override
	public ARGBType getColor(String key) {
		return colors.get(key);
	}
}
