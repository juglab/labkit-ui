package net.imglib2.atlas.control.brush;

import java.util.Iterator;

import net.imglib2.Localizable;
import net.imglib2.Point;

public class IteratorOverBackMappingPoints< T, S extends Iterator< T > & Localizable > extends Point implements Iterator< T >, Localizable
{

	final S c;

	private final int[] backMapDims;

	public IteratorOverBackMappingPoints( final S c, final Localizable pos, final int... backMapDims )
	{
		super( pos );
		this.c = c;
		this.backMapDims = backMapDims;
	}

	@Override
	public boolean hasNext()
	{
		return c.hasNext();
	}

	@Override
	public T next()
	{
		final T t = c.next();
		for ( int d = 0; d < backMapDims.length; ++d )
			setPosition( c.getLongPosition( d ), backMapDims[ d ] );
		return t;
	}

}