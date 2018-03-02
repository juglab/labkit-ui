package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.AbstractLocalizable;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RealPositionable;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import net.imglib2.type.logic.BitType;

import java.util.Iterator;

/**
 * TODO
 *
 * @author Tobias Pietzsch
 * @author Stephan Preibisch
 * @author Stephan Saalfeld
 * @author Matthias Arzt
 */
public class EllipsoidNeighborhood< T > extends AbstractLocalizable implements Neighborhood< T >
{
	private final RandomAccess< T > source;

	private final Interval boundingBox;

	private final IterableRegion< BitType > region;

	public EllipsoidNeighborhood(long position[], AffineTransform3D transformation, final RandomAccess<T> source )
	{
		super(position);
		if(source.numDimensions() != 3)
			throw new IllegalArgumentException( "Only support 3d images" );
		this.source = source;
		TransformedSphere sphere = new TransformedSphere( transformation );
		this.boundingBox = sphere.boundingBox();
		this.region = TransformedSphere.iterableRegion( sphere );
	}

	@Override public Interval getStructuringElementBoundingBox()
	{
		return boundingBox;
	}

	@Override public Cursor< T > cursor()
	{
		return new MappingCursor<>( position, region.cursor(), source.copyRandomAccess() );
	}

	@Override public Cursor< T > localizingCursor()
	{
		return cursor();
	}

	@Override public long size()
	{
		return region.size();
	}

	@Override public T firstElement()
	{
		return cursor().next();
	}

	@Override public Object iterationOrder()
	{
		return this;
	}

	@Override public Iterator< T > iterator()
	{
		return cursor();
	}

	@Override public long min( int d )
	{
		return boundingBox.min( d ) + position[ d ];
	}

	@Override public void min( long[] min )
	{
		for( int d = 0; d < numDimensions(); d++ )
			min[ d ] = min( d );
	}

	@Override public void min( Positionable min )
	{
		for( int d = 0; d < numDimensions(); d++ )
			min.setPosition( min( d ), d );
	}

	@Override public long max( int d )
	{
		return boundingBox.max( d ) + position[ d ];
	}

	@Override public void max( long[] max )
	{
		for( int d = 0; d < numDimensions(); d++ )
			max[ d ] = max( d );
	}

	@Override public void max( Positionable max )
	{
		for( int d = 0; d < numDimensions(); d++ )
			max.setPosition( max( d ), d );
	}

	@Override public void dimensions( long[] dimensions )
	{
		boundingBox.dimensions( dimensions );
	}

	@Override public long dimension( int d )
	{
		return boundingBox.dimension( d );
	}

	@Override public double realMin( int d )
	{
		return boundingBox.realMin( d ) + position[ d ];
	}

	@Override public void realMin( double[] min )
	{
		for( int d = 0; d < numDimensions(); d++ )
			min[ d ] = realMin( d );
	}

	@Override public void realMin( RealPositionable min )
	{
		for( int d = 0; d < numDimensions(); d++ )
			min.setPosition( realMin( d ), d );
	}

	@Override public double realMax( int d )
	{
		return boundingBox.realMax( d ) + position[ d ];
	}

	@Override public void realMax( double[] max )
	{
		for( int d = 0; d < numDimensions(); d++ )
			max[ d ] = realMax( d );
	}

	@Override public void realMax( RealPositionable max )
	{
		for( int d = 0; d < numDimensions(); d++ )
			max.setPosition( realMax( d ), d );
	}

}
