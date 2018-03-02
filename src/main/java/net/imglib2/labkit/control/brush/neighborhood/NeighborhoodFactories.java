package net.imglib2.labkit.control.brush.neighborhood;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.HyperSphereNeighborhood;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.realtransform.AffineTransform3D;

import java.util.stream.DoubleStream;

public class NeighborhoodFactories
{

	public static < T > NeighborhoodFactory hyperSphere()
	{
		return new NeighborhoodFactory() {
			@Override
			public <T> Neighborhood<T> create(RandomAccess<T> access, long[] position, long size) {
				return HyperSphereNeighborhood.<T>factory().create(position, size, access);
			}
		};
	}

	public static NeighborhoodFactory hyperEllipsoid() {
		return (new NeighborhoodFactory() {
			@Override
			public <T> Neighborhood<T> create(RandomAccess<T> access, long[] position, long size) {
				AffineTransform3D transformation = new AffineTransform3D();
				transformation.set(5.0, 0.0, 0.0, 0.0,
						0.0, 10.0, 0.0, 0.0,
						0.0, 0.0, 20, 0.0);
				return new EllipsoidNeighborhood<>( position, transformation, access);
			}
		});
	}
}
