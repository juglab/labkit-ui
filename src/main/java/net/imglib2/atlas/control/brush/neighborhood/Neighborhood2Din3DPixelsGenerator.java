package net.imglib2.atlas.control.brush.neighborhood;

import bdv.util.Affine3DHelpers;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealLocalizable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.Type;
import net.imglib2.view.MixedTransformView;
import net.imglib2.view.Views;

public class Neighborhood2Din3DPixelsGenerator< T extends Type< T > >
		implements PaintPixelsGenerator< T, IteratorOverBackMappingPoints< T, Cursor< T > > >
{

	private final NeighborhoodFactory< T > fac;

	private final int normalAxis;

	private final AffineTransform3D transform;

	public Neighborhood2Din3DPixelsGenerator( final AffineTransform3D transform )
	{
		this( NeighborhoodFactories.hyperSphere(), 2, transform );
	}

	public Neighborhood2Din3DPixelsGenerator( final NeighborhoodFactory< T > fac, final int normalAxis, final AffineTransform3D transform )
	{
		super();
		this.fac = fac;
		this.normalAxis = normalAxis;
		this.transform = transform;
	}

	@Override
	public IteratorOverBackMappingPoints< T, Cursor< T > > getPaintPixels( final RandomAccessible< T > accessible, final RealLocalizable position, final int timestep, final int size )
	{
		assert accessible.numDimensions() == 3;
		final long alongNormalPosition = Math.round( position.getDoublePosition( normalAxis ) );
		final int backMapDim1 = normalAxis == 0 ? 1 : 0;
		final int backMapDim2 = normalAxis == 2 ? 1 : 2;
		final MixedTransformView< T > slice = Views.hyperSlice( accessible, normalAxis, alongNormalPosition );
		final RandomAccess< T > access = slice.randomAccess();
		final long[] neighborhoodPosition = {
				Math.round( position.getDoublePosition( backMapDim1 ) ),
				Math.round( position.getDoublePosition( backMapDim2 ) )
		};

		final long scaledSize = Math.round( size / Affine3DHelpers.extractScale( transform, normalAxis == 0 ? 1 : 0 ) );

		final Neighborhood< T > neighborhood = fac.create( access, neighborhoodPosition, scaledSize );

		final RandomAccess< T > backMappedPosition = accessible.randomAccess();
		backMappedPosition.setPosition( alongNormalPosition, normalAxis );
		return new IteratorOverBackMappingPoints<>( neighborhood.cursor(), backMappedPosition, backMapDim1, backMapDim2 );
	}

}
