package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransformedSphereTest
{
	private final AffineTransform3D transform = transform( 0.1, 3, -2, 0.6, 1, 3, 4, 5, -2, -5, -0.6, 8);

	@Test
	public void testTransformedSphereOutside() {
		List< RealLocalizable > notContained = Arrays.asList( new RealPoint(1.0, 1.0, 1.0), new RealPoint(0, 1.1, 0) );
		TransformedSphere sphere = new TransformedSphere( transform );
		for( RealLocalizable x : transform( transform, notContained ) )
			assertFalse( sphere.contains( x ) );
	}

	@Test
	public void testTransformedSphereInside() {
		List< RealLocalizable > contained = Arrays.asList( new RealPoint(0, 0, 0), new RealPoint(0, 1, 0) );
		TransformedSphere sphere = new TransformedSphere( transform );
		for( RealLocalizable x : transform( transform, contained ) )
			assertTrue( sphere.contains( x ) );
	}

	@Test
	public void testTransformedSphereBoundingBox() {
		TransformedSphere sphere = new TransformedSphere(
				transform(
						1.0, 0.0, 0.0, 4.0,
						0.0, -2.0, 0.0, 0.0,
						-1.0, 0.0, 3.0, -7.0) );
		Interval result = sphere.boundingBox();
		assertTrue( Intervals.equals( Intervals.createMinMax( 3, -2, -11, 5, 2, -3 ), result ) );
	}

	@Test
	public void testBitmap() {
		TransformedSphere sphere = new TransformedSphere( transform(
						2, 0, 0, 0,
						0, 1, 0, 0,
						0, 0, 1, 0) );
		RandomAccessibleInterval< BitType > bitmap = TransformedSphere.bitmap( sphere );
		IntervalView< BitType > bitmapSlice = Views.hyperSlice( bitmap, 1, 0 );
		Img< IntType > expected = ArrayImgs.ints(
				new int[] { 0, 0, 1, 0, 0,
				         1, 1, 1, 1, 1,
				         0, 0, 1, 0, 0 },
				5, 3 );
		LoopBuilder.setImages( expected, bitmapSlice ).forEachPixel( (e, a) -> assertEquals(e.getInteger(), a.getInteger()) );
	}

	private static AffineTransform3D transform( double... values )
	{
		assert values.length == 12;
		AffineTransform3D transform = new AffineTransform3D();
		transform.set( values );
		return transform;
	}

	private RealLocalizable transform( AffineTransform3D transform, RealLocalizable point )
	{
		RealPoint result = new RealPoint( point.numDimensions() );
		transform.apply( point, result );
		return result;
	}

	private List< RealLocalizable > transform( AffineTransform3D transform, List< RealLocalizable > point )
	{
		return point.stream().map( x -> transform( transform, x )).collect( Collectors.toList());
	}
}
