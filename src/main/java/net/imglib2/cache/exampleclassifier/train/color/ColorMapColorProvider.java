package net.imglib2.cache.exampleclassifier.train.color;

import java.awt.Color;
import java.util.Random;
import java.util.stream.IntStream;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import net.imglib2.type.numeric.IntegerType;

public class ColorMapColorProvider< I extends IntegerType< I > > implements IntegerColorProvider< I >
{
	public static int RGB_BITS = 255 << 16 | 255 << 8 | 255 << 0;

	private final TIntIntHashMap colors;

	// use full opacity for now
	private final int ALPHA_BITS = 255 << 24;

	public ColorMapColorProvider( final TIntIntHashMap colors )
	{
		super();
		this.colors = colors;
	}

	public ColorMapColorProvider( final Random rng, final int noEntryKey, final int noEntryValue, final int... keys )
	{
		this( new TIntIntHashMap( Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, noEntryKey, noEntryValue ) );
		setColors( rng, keys );
	}

	// will make new copy
	public void setColors( final TIntIntMap colors )
	{
		this.colors.clear();
		this.colors.putAll( colors );
	}

	public void setColors( final Random rng, final int... keys )
	{
		final float step = 1.0f / keys.length;
		final float start = rng.nextFloat();
		final int[] values = IntStream
				.range( 0, keys.length )
				.mapToDouble( i -> start + step * i )
				.map( d -> d > 1.0 ? d - 1.0 : d )
				.mapToInt( d -> Color.HSBtoRGB( ( float ) d, 1.0f, 1.0f ) ).toArray();
		setColors( keys, values );

	}

	public void setColors( final int[] keys, final int[] values )
	{
		this.colors.clear();
		for ( int i = 0; i < keys.length; ++i )
			this.colors.put( keys[ i ], values[ i ] );
	}

	@Override
	public int getColor( final int i )
	{
		return ( colors.get( i ) | ALPHA_BITS ) & RGB_BITS;
	}

	@Override
	public int getColor( final long l )
	{
		return getColor( ( int ) l );
	}

}
