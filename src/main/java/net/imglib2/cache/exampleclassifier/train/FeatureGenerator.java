package net.imglib2.cache.exampleclassifier.train;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public interface FeatureGenerator< S extends RealType< S >, T extends RealType< T > >
{
	public void generateFeatures( RandomAccessible< S > source, RandomAccessibleInterval< T > target );

	public int numFeatures( int numDimensions );

	default public int numFeatures( final Interval interval )
	{
		return numFeatures( interval.numDimensions() );
	}

	public default FeatureGenerator< S, T > copy()
	{
		return this;
	}

}
