package net.imglib2.cache.exampleclassifier.train2;

import java.util.Arrays;
import java.util.stream.LongStream;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

public interface FeatureGenerator< S extends RealType< S >, T extends RealType< T > >
{
	public void generateFeatures( RandomAccessibleInterval< T > target ) throws Exception;


	default public int numFeatures()
	{
		return 1;
	}

	default public Interval targetSize( final Interval interval )
	{
		final long[] min = LongStream.concat( Arrays.stream( Intervals.minAsLongArray( interval ) ), LongStream.of( 0 ) ).toArray();
		final long[] max = LongStream.concat( Arrays.stream( Intervals.maxAsLongArray( interval ) ), LongStream.of( numFeatures() - 1 ) ).toArray();
		return new FinalInterval( min, max );
	}

	default public FeatureGenerator< S, T > copy()
	{
		return this;
	}

}
