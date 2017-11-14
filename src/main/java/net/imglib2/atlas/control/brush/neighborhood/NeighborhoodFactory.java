package net.imglib2.atlas.control.brush.neighborhood;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;

public interface NeighborhoodFactory {
	< T > Neighborhood< T > create( RandomAccess< T > access, long[] position, long size );
}
