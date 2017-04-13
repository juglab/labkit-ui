package net.imglib2.atlas;

import java.util.HashSet;

import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;

public class RealRandomAccessibleContainer< T > implements RealRandomAccessible< T >
{

	public static interface SourceChangeListener< T >
	{
		public void notifyOnSourceChange( RealRandomAccessible< T > oldSource, RealRandomAccessible< T > newSource );
	}

	private RealRandomAccessible< T > source;

	private final HashSet< SourceChangeListener< T > > listeners;

	public RealRandomAccessibleContainer( final RealRandomAccessible< T > source )
	{
		super();
		this.source = source;
		this.listeners = new HashSet<>();
	}

	public void setSource( final RealRandomAccessible< T > source )
	{
		listeners.forEach( l -> l.notifyOnSourceChange( this.source, source ) );
		this.source = source;
	}

	public void addListener( final SourceChangeListener< T > listener )
	{
		this.listeners.add( listener );
	}

	public boolean removeListener( final SourceChangeListener< T > listener )
	{
		return this.listeners.remove( listener );
	}



	@Override
	public RealRandomAccess< T > realRandomAccess()
	{
		return source.realRandomAccess();
	}

	@Override
	public RealRandomAccess< T > realRandomAccess( final RealInterval interval )
	{
		return source.realRandomAccess( interval );
	}

	@Override
	public int numDimensions()
	{
		return source.numDimensions();
	}

}
