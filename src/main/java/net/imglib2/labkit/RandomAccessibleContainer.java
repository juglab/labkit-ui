package net.imglib2.labkit;

import java.util.HashSet;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;

public class RandomAccessibleContainer< T > implements RandomAccessible< T >
{

	public RandomAccessible<T> getSource() {
		return source;
	}

	public static interface SourceChangeListener< T >
	{
		public void notifyOnSourceChange( RandomAccessible< T > oldSource, RandomAccessible< T > newSource );
	}

	private RandomAccessible< T > source;

	private final HashSet< SourceChangeListener< T > > listeners;

	public RandomAccessibleContainer( final RandomAccessible< T > source )
	{
		super();
		this.source = source;
		this.listeners = new HashSet<>();
	}

	public void setSource( final RandomAccessible< T > source )
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
	public RandomAccess< T > randomAccess()
	{
		return source.randomAccess();
	}

	@Override
	public RandomAccess< T > randomAccess( final Interval interval )
	{
		return source.randomAccess( interval );
	}

	@Override
	public int numDimensions()
	{
		return source.numDimensions();
	}

}
