package net.imglib2.atlas;

import java.util.Comparator;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

public class ArgOptimumLoader< T extends RealType< T > > implements CellLoader< UnsignedShortType >
{
	private final CellGrid grid;

	private final RandomAccessible< ? extends Composite< T > > map;

	private final int size;

	private final Comparator< T > comp;

	private final T worstVal;

	public ArgOptimumLoader(
			final CellGrid grid,
			final RandomAccessible< ? extends Composite< T > > features,
					final int size,
					final Comparator< T > comp,
					final T worstVal )
	{
		this.grid = grid;
		this.map = features;
		this.size = size;
		this.comp = comp;
		this.worstVal = worstVal;
	}

	public ArgOptimumLoader(
			final CellGrid grid,
			final RandomAccessible< ? extends Composite< T > > features,
					final int size,
					final T worstVal )
	{
		this( grid, features, size, Comparator.reverseOrder(), worstVal );
	}

	@Override
	public void load( final Img< UnsignedShortType > img ) throws Exception
	{

		final long[] cellMin = Intervals.minAsLongArray( img );
		final long[] cellMax = Intervals.maxAsLongArray( img );
		final FinalInterval sourceCellInterval = new FinalInterval( cellMin, cellMax );
		final Cursor< ? extends Composite< T > > instancesCursor = Views.flatIterable( Views.interval( map, sourceCellInterval ) ).cursor();
		final Cursor< UnsignedShortType > imgCursor = Views.flatIterable( img ).cursor();
		final T argOptimum = worstVal.copy();
		while ( imgCursor.hasNext() )
		{
			argOptimum.set( worstVal );
			final Composite< T > pred = instancesCursor.next();
			final UnsignedShortType target = imgCursor.next();
			int arg = 0;
			for ( int d = 0; d < size; ++d )
			{
				final T comparison = pred.get( d );
				if ( comp.compare( comparison, argOptimum ) < 0 )
				{
					arg = d;
					argOptimum.set( comparison );
				}
			}
			target.set( arg );
		}

	}
}