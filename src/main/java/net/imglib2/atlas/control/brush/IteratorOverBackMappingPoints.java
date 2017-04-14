package net.imglib2.atlas.control.brush;

import java.util.Iterator;

import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;

public class IteratorOverBackMappingPoints< T > implements Iterator< T >, Localizable
{

	final Cursor< T > c;

	private final RandomAccess< T > access;

	private final int backMapDim1;

	private final int backMapDim2;

	public IteratorOverBackMappingPoints( final Cursor< T > c, final RandomAccess< T > access, final int backMapDim1, final int backMapDim2 )
	{
		super();
		this.c = c;
		this.access = access;
		this.backMapDim1 = backMapDim1;
		this.backMapDim2 = backMapDim2;
	}

	@Override
	public boolean hasNext()
	{
		return c.hasNext();
	}

	@Override
	public T next()
	{
		c.fwd();
		access.setPosition( c.getLongPosition( 0 ), backMapDim1 );
		access.setPosition( c.getLongPosition( 1 ), backMapDim2 );
		return access.get();
	}

	@Override
	public void localize( final float[] position )
	{
		access.localize( position );
	}

	@Override
	public void localize( final double[] position )
	{
		access.localize( position );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		return access.getFloatPosition( d );
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return access.getDoublePosition( d );
	}

	@Override
	public int numDimensions()
	{
		return access.numDimensions();
	}

	@Override
	public void localize( final int[] position )
	{
		access.localize( position );
	}

	@Override
	public void localize( final long[] position )
	{
		access.localize( position );
	}

	@Override
	public int getIntPosition( final int d )
	{
		return access.getIntPosition( d );
	}

	@Override
	public long getLongPosition( final int d )
	{
		return access.getLongPosition( d );
	}

}