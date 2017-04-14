package net.imglib2.atlas.control.brush;

import java.util.stream.IntStream;

import bdv.util.Affine3DHelpers;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.Type;

public class Neighborhood3DPixelsGenerator< T extends Type< T > >
implements PaintPixelsGenerator< T, Cursor< T > >
{

	private final NeighborhoodFactory< T > fac;

	private final AffineTransform3D transform;

	public Neighborhood3DPixelsGenerator( final AffineTransform3D transform )
	{
		this( NeighborhoodFactories.hyperSphere(), transform );
	}

	public Neighborhood3DPixelsGenerator( final NeighborhoodFactory< T > fac, final AffineTransform3D transform )
	{
		super();
		this.fac = fac;
		this.transform = transform;
	}

	@Override
	public Cursor< T > getPaintPixels( final RandomAccessible< T > accessible, final RealLocalizable position, final int timestep, final int size )
	{
		assert accessible.numDimensions() == 3;
		final RandomAccess< T > access = accessible.randomAccess();
		final long scaledSize = Math.round( size / Affine3DHelpers.extractScale( transform, 0 ) );
		final long[] pos = IntStream.range( 0, 3 ).mapToLong( d -> Math.round( position.getDoublePosition( d ) ) ).toArray();
		final Neighborhood< T > neighborhood = fac.create( access, pos, scaledSize );
		return neighborhood.cursor();
	}

}
