package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.IterableRegion;
import net.imglib2.sparse.SparseIterableRegion;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class TransformedSphere
{

	private final AffineTransform3D transform;

	public TransformedSphere( AffineTransform3D transform )
	{
		this.transform = transform;
	}

	public boolean contains( RealLocalizable point ) {
		RealPoint out = new RealPoint( point.numDimensions() );
		transform.applyInverse( out, point );
		return RealPoints.squaredLength( out ) <= 1;
	}

	public RealInterval realBoundingBox() {
		double[] min = new double[3];
		double[] max = new double[3];
		for( int d = 0; d < 3; d++ ) {
			double halfLength = Math.abs( transform.get( d, 0 ) ) + Math.abs( transform.get( d, 1 ) ) + Math.abs( transform.get( d, 2 ) );
			double center = transform.get( d, 3 );
			min[ d ] = center - halfLength;
			max[ d ] = center + halfLength;
		}
		return new FinalRealInterval( min, max );
	}

	public Interval boundingBox() {
		RealInterval boundingBox = realBoundingBox();
		long[] min = new long[ boundingBox.numDimensions() ];
		long[] max = new long[ boundingBox.numDimensions() ];
		for( int d = 0; d < boundingBox.numDimensions(); d++ ) {
			min[ d ] = (long) Math.floor( boundingBox.realMin( d ) );
			max[ d ] = (long) Math.ceil( boundingBox.realMax( d ) );
		}
		return new FinalInterval( min, max );
	}

	public static RandomAccessibleInterval< BitType > bitmap(TransformedSphere sphere) {
		Interval interval = sphere.boundingBox();
		IntervalView< BitType > result = Views.translate( ArrayImgs.bits( Intervals.dimensionsAsLongArray( interval )), Intervals.minAsLongArray( interval ) );
		Cursor< BitType > cursor = result.cursor();
		while ( cursor.hasNext() ) {
			cursor.fwd();
			cursor.get().set( sphere.contains( cursor ) );
		}
		return result;
	}

	public static IterableRegion< BitType > iterableRegion(TransformedSphere sphere) {
		Interval interval = sphere.boundingBox();
		SparseIterableRegion result = new SparseIterableRegion( interval );
		Cursor< BitType > cursor = Views.flatIterable( result ).cursor();
		while ( cursor.hasNext() ) {
			cursor.fwd();
			cursor.get().set( sphere.contains( cursor ) );
		}
		return result;
	}
}
