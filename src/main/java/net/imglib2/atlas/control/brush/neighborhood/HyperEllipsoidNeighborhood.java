package net.imglib2.atlas.control.brush.neighborhood;

import java.util.Iterator;

import net.imglib2.AbstractEuclideanSpace;
import net.imglib2.AbstractLocalizable;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.algorithm.neighborhood.Neighborhood;

/**
 * TODO
 *
 * @author Tobias Pietzsch
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Matthias Arzt
 */
public class HyperEllipsoidNeighborhood< T > extends AbstractLocalizable implements Neighborhood< T >
{
	private final RandomAccess< T > sourceRandomAccess;

	private final double[] radius;

	private final int maxDim;

	private final long size;

	private final Interval structuringElementBoundingBox;

	public HyperEllipsoidNeighborhood(final long[] position, final double[] radius, final RandomAccess<T> sourceRandomAccess)
	{
		super( position );
		this.sourceRandomAccess = sourceRandomAccess;
		this.radius = radius;
		maxDim = n - 1;
		size = computeSize();

		final long[] min = new long[ n ];
		final long[] max = new long[ n ];

		for ( int d = 0; d < n; d++ )
		{
			min[ d ] = - (long) radius[ d ];
			max[ d ] = (long) radius[ d ];
		}

		structuringElementBoundingBox = new FinalInterval( min, max );
	}

	/**
	 * Compute the number of elements for iteration
	 */
	protected long computeSize()
	{
		final LocalCursor cursor = new LocalCursor( sourceRandomAccess );

		// "compute number of pixels"
		long size = 0;
		while ( cursor.hasNext() )
		{
			cursor.fwd();
			++size;
		}

		return size;
	}

	public final class LocalCursor extends AbstractEuclideanSpace implements Cursor< T >
	{
		private final RandomAccess< T > source;

		// the current radius in each dimension we are at
		private final double[] r;

		// the current radius in each dimension truncated to long
		private final long[] ri;

		// the remaining number of steps in each dimension we still have to go
		private final long[] s;

		public LocalCursor( final RandomAccess< T > source )
		{
			super( source.numDimensions() );
			this.source = source;
			r = new double[ n ];
			ri = new long[ n ];
			s = new long[ n ];
			reset();
		}

		protected LocalCursor( final LocalCursor c )
		{
			super( c.numDimensions() );
			source = c.source.copyRandomAccess();
			r = c.r.clone();
			ri = c.ri.clone();
			s = c.s.clone();
		}

		@Override
		public T get()
		{
			return source.get();
		}

		@Override
		public void fwd()
		{

			if ( --s[ 0 ] >= 0 )
				source.fwd( 0 );
			else
			{
				int d = 1;
				for ( ; d < n; ++d )
				{
					if ( --s[ d ] >= 0 )
					{
						source.fwd( d );
						break;
					}
				}

				for ( ; d > 0; --d )
				{
					final int e = d - 1;
					final double rd = r[ d ];
					final double pd = (s[ d ] - ri[ d ]) / radius[ d ];

					final double rad = Math.sqrt( rd * rd - pd * pd );
					final long radi = ( long ) (rad * radius[ e ]);
					r[ e ] = rad;
					ri[ e ] = radi;
					s[ e ] = 2 * radi;

					source.setPosition( position[ e ] - radi, e );
				}
			}
		}

		@Override
		public void jumpFwd( final long steps )
		{
			for ( long i = 0; i < steps; ++i )
				fwd();
		}

		@Override
		public T next()
		{
			fwd();
			return get();
		}

		@Override
		public void remove()
		{
			// NB: no action.
		}

		@Override
		public void reset()
		{
			for ( int d = 0; d < maxDim; ++d )
			{
				r[ d ] = ri[ d ] = s[ d ] = 0;
				source.setPosition( position[ d ], d );
			}

			r[ maxDim ] = 1.0;
			ri[ maxDim ] = (long) radius[ maxDim ];
			s[ maxDim ] = 1 + 2 * ri[ maxDim ];

			source.setPosition( position[ maxDim ] - ri[ maxDim ] - 1, maxDim );

		}

		@Override
		public boolean hasNext()
		{
			for ( int d = maxDim; d >= 0; --d )
				if( s[ d ] > 0 )
					return true;
			return false;
		}

		@Override
		public float getFloatPosition( final int d )
		{
			return source.getFloatPosition( d );
		}

		@Override
		public double getDoublePosition( final int d )
		{
			return source.getDoublePosition( d );
		}

		@Override
		public int getIntPosition( final int d )
		{
			return source.getIntPosition( d );
		}

		@Override
		public long getLongPosition( final int d )
		{
			return source.getLongPosition( d );
		}

		@Override
		public void localize( final long[] position )
		{
			source.localize( position );
		}

		@Override
		public void localize( final float[] position )
		{
			source.localize( position );
		}

		@Override
		public void localize( final double[] position )
		{
			source.localize( position );
		}

		@Override
		public void localize( final int[] position )
		{
			source.localize( position );
		}

		@Override
		public LocalCursor copy()
		{
			return new LocalCursor( this );
		}

		@Override
		public LocalCursor copyCursor()
		{
			return copy();
		}
	}

	@Override
	public Interval getStructuringElementBoundingBox()
	{
		return structuringElementBoundingBox;
	}

	@Override
	public long size()
	{
		return size;
	}

	@Override
	public T firstElement()
	{
		return cursor().next();
	}

	@Override
	public Object iterationOrder()
	{
		return this; // iteration order is only compatible with ourselves
	}

	@Override
	public double realMin( final int d )
	{
		return position[ d ] - radius[ d ];
	}

	@Override
	public void realMin( final double[] min )
	{
		for ( int d = 0; d < min.length; d++ )
		{
			min[ d ] = realMin( d );
		}
	}

	@Override
	public void realMin( final RealPositionable min )
	{
		for ( int d = 0; d < min.numDimensions(); d++ )
		{
			min.setPosition( realMin( d ), d );
		}
	}

	@Override
	public double realMax( final int d )
	{
		return position[ d ] + radius[ d ];
	}

	@Override
	public void realMax( final double[] max )
	{
		for ( int d = 0; d < max.length; d++ )
		{
			max[ d ] = realMax( d );
		}
	}

	@Override
	public void realMax( final RealPositionable max )
	{
		for ( int d = 0; d < max.numDimensions(); d++ )
		{
			max.setPosition( realMax( d ), d );
		}
	}

	@Override
	public Iterator< T > iterator()
	{
		return cursor();
	}

	@Override
	public long min( final int d )
	{
		return position[ d ] - (long) radius[d];
	}

	@Override
	public void min( final long[] min )
	{
		for ( int d = 0; d < min.length; d++ )
		{
			min[ d ] = min( d );
		}
	}

	@Override
	public void min( final Positionable min )
	{
		for ( int d = 0; d < min.numDimensions(); d++ )
		{
			min.setPosition( min( d ), d );
		}
	}

	@Override
	public long max( final int d )
	{
		return position[ d ] + (long) radius[ d ];
	}

	@Override
	public void max( final long[] max )
	{
		for ( int d = 0; d < max.length; d++ )
		{
			max[ d ] = max( d );
		}
	}

	@Override
	public void max( final Positionable max )
	{
		for ( int d = 0; d < max.numDimensions(); d++ )
		{
			max.setPosition( max(d), d );
		}
	}

	@Override
	public void dimensions( final long[] dimensions )
	{
		for ( int d = 0; d < dimensions.length; d++ )
		{
			dimensions[ d ] = dimension( d );
		}
	}

	@Override
	public long dimension( final int d )
	{
		return (long) ( 2 * radius[d] ) + 1;
	}

	@Override
	public LocalCursor cursor()
	{
		return new LocalCursor( sourceRandomAccess.copyRandomAccess() );
	}

	@Override
	public LocalCursor localizingCursor()
	{
		return cursor();
	}

}
