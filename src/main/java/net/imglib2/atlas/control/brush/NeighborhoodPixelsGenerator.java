package net.imglib2.atlas.control.brush;

import java.util.stream.IntStream;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.type.Type;

public class NeighborhoodPixelsGenerator< T extends Type< T > >
implements PaintPixelsGenerator< T, Cursor< T > >
{

	private final NeighborhoodFactory< T > fac;

	private final double labelsScale;

	public NeighborhoodPixelsGenerator( final double labelsScale )
	{
		this( NeighborhoodFactories.hyperSphere(), labelsScale );
	}

	public NeighborhoodPixelsGenerator( final NeighborhoodFactory< T > fac, final double labelsScale )
	{
		super();
		this.fac = fac;
		this.labelsScale = labelsScale;
	}

	@Override
	public Cursor< T > getPaintPixels( final RandomAccessible< T > accessible, final RealLocalizable position, final int timestep, final int size )
	{
		assert accessible.numDimensions() == position.numDimensions();
		final int nDim = accessible.numDimensions();
		final RandomAccess< T > access = accessible.randomAccess();
		final long scaledSize = Math.round( size / labelsScale );
		final long[] pos = IntStream.range( 0, nDim ).mapToLong( d -> Math.round( position.getDoublePosition( d ) ) ).toArray();
		final Neighborhood< T > neighborhood = fac.create( access, pos, scaledSize );
		return neighborhood.cursor();
	}

}
