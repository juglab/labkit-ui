package net.imglib2.cache.exampleclassifier.train;

import java.util.Comparator;
import java.util.stream.IntStream;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.cache.CacheLoader;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;

public class ArgOptimumLoader< T extends RealType< T > > implements CacheLoader< Long, Cell< VolatileShortArray > >
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
	public Cell< VolatileShortArray > get( final Long key ) throws Exception
	{
		final long index = key;

		final int n = grid.numDimensions();
		final long[] cellMin = new long[ n ];
		final int[] cellDims = new int[ n ];
		grid.getCellDimensions( index, cellMin, cellDims );
		final long[] cellMax = IntStream.range( 0, n ).mapToLong( d -> cellMin[ d ] + cellDims[ d ] - 1 ).toArray();

		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final VolatileShortArray array = new VolatileShortArray( blocksize, true );

		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( array.getCurrentStorageArray(), Util.int2long( cellDims ) );

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

		return new Cell<>( cellDims, cellMin, array );
	}
}