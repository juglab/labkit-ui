package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.util.Pair;
import net.imglib2.util.ValuePair;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SphereTest
{
	@Test
	public void testSphereContains() {
		Sphere sphere = new Sphere();
		assertTrue( sphere.contains( new RealPoint(0, 0, 0) ) );
		assertTrue( sphere.contains( new RealPoint(0, 1, 0) ) );
		assertFalse( sphere.contains( new RealPoint(0, 0, 1.001) ) );
	}

	@Test
	public void testIntersections() {
		Sphere sphere = new Sphere();
		Pair<RealLocalizable, RealLocalizable> pair = sphere.intersection( new RealPoint(0,0,0), new RealPoint(1,0,0) );
		RealPoints.assertEquals( new RealPoint( -1, 0, 0 ), pair.getA() );
		RealPoints.assertEquals( new RealPoint( 1, 0, 0 ), pair.getB() );
	}

	@Test
	public void testIntersections1() {
		Sphere sphere = new Sphere();
		Pair<RealLocalizable, RealLocalizable> pair = sphere.intersection( new RealPoint(1,0,8.4), new RealPoint(1,0,1) );
		RealPoints.assertEquals( new RealPoint( 1, 0, 0 ), pair.getA() );
		RealPoints.assertEquals( new RealPoint( 1, 0, 0 ), pair.getB() );
	}

	@Test
	public void testIntersections2() {
		Sphere sphere = new Sphere();
		Pair<RealLocalizable, RealLocalizable> pair = sphere.intersection( new RealPoint(0.5,0.5,0), new RealPoint(0.5,0.5,9) );
		RealPoints.assertEquals( new RealPoint( 0.5, 0.5, - Math.sqrt( 0.5 ) ), pair.getA(), 0.0001 );
		RealPoints.assertEquals( new RealPoint( 0.5, 0.5, Math.sqrt( 0.5 ) ), pair.getB(), 0.0001 );
	}

	@Test
	public void testIntersectionsMissing() {
		Sphere sphere = new Sphere();
		Pair<RealLocalizable, RealLocalizable> pair = sphere.intersection( new RealPoint(2, 2, 2), new RealPoint(2, 2, 3) );
		assertNull(pair);
	}

	private static class Sphere {

		public boolean contains( RealLocalizable point ) {
			return RealPoints.squaredLength( point ) <= 1.0;
		}

		public Pair<RealLocalizable, RealLocalizable> intersection( RealPoint a, RealPoint b )
		{
			RealPoint d = RealPoints.subtract( a, b );
			double skalarProdukt = RealPoints.skalarProdukt( b, d );
			double squaredLengthD = RealPoints.squaredLength( d );
			double squaredProjectionLength = RealPoints.sqr( skalarProdukt ) / squaredLengthD;
			double squaredOrthogonalLength = RealPoints.squaredLength( b ) - squaredProjectionLength;
			if(squaredOrthogonalLength > 1.0)
				return null;
			double scale = skalarProdukt / squaredLengthD;
			double requiredScale = Math.sqrt( (1 - squaredOrthogonalLength) / squaredLengthD );
			RealPoint first = RealPoints.add( b, RealPoints.scale( - scale + requiredScale, d ) );
			RealPoint second = RealPoints.add( b, RealPoints.scale( - scale - requiredScale, d ) );
			return new ValuePair<>( first, second );
		}

	}
}
