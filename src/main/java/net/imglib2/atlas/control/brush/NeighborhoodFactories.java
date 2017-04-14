package net.imglib2.atlas.control.brush;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.HyperSphereNeighborhood;
import net.imglib2.algorithm.neighborhood.HyperSphereNeighborhoodFactory;
import net.imglib2.algorithm.neighborhood.Neighborhood;

public class NeighborhoodFactories
{

	public static class HypherSphereNeighborhoodFactory< T > implements NeighborhoodFactory< T >
	{

		private final HyperSphereNeighborhoodFactory< T > fac = HyperSphereNeighborhood.< T >factory();

		@Override
		public Neighborhood< T > create( final RandomAccess< T > access, final long[] position, final long size )
		{
			final Neighborhood< T > sphere =
					fac.create(
							position,
							size,
							access );
			return sphere;
		}

	}

	public static < T > NeighborhoodFactory< T > hyperSphere()
	{
		return new HypherSphereNeighborhoodFactory<>();
	}

}
