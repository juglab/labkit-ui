package net.imglib2.cache.exampleclassifier.train;

import net.imglib2.AbstractWrappedInterval;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

public class MouseWheelSelectorRandomAccessibleInterval< T > extends AbstractWrappedInterval< Interval > implements RandomAccessibleInterval< T >
{
	private final RandomAccessibleInterval< T > source;

	private final int d;

	private final long minSlice;

	private final long maxSlice;

	private long slice;

	public MouseWheelSelectorRandomAccessibleInterval( final RandomAccessibleInterval< T > source, final int d )
	{
		super( new FinalInterval( Views.hyperSlice( source, d, source.min( d ) ) ) );
		this.source = source;
		this.d = d;
		this.minSlice = source.min( d );
		this.maxSlice = source.max( d );
		this.slice = minSlice;
	}

	@Override
	public RandomAccess< T > randomAccess()
	{
		return Views.hyperSlice( source, d, slice ).randomAccess();
	}

	@Override
	public RandomAccess< T > randomAccess( final Interval interval )
	{
		return Views.hyperSlice( source, d, slice ).randomAccess( interval );
	}

	public long getSliceIndex()
	{
		return slice;
	}

	public long getMinSlice()
	{
		return minSlice;
	}

	public long getMaxSlice()
	{
		return maxSlice;
	}

	public void setsSlice( final long slice )
	{
		this.slice = slice;
	}

}
