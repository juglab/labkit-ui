package net.imglib2.atlas.classification;

import java.util.Arrays;
import java.util.stream.LongStream;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.volatiles.VolatileShortAccess;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class ClassifyingCellLoader< T extends RealType< T > > implements CellLoader< ShortType >
{

	public static interface ShortAccessGenerator< A extends VolatileShortAccess > {
		public A create( long numEntities, boolean isValid );
	}

	private final RandomAccessible< T > features;

	private Classifier classifier;

	private final int numFeatures;

	public void setClassifier( final Classifier classifier )
	{
		this.classifier = classifier;
	}

	public ClassifyingCellLoader(
			final RandomAccessibleInterval<T> features,
			final Classifier classifier)
	{
		this.features = features;
		this.classifier = classifier;
		this.numFeatures = (int) features.dimension(features.numDimensions() - 1);
	}

	@Override
	public void load( final Img< ShortType > img ) throws Exception
	{

		final long[] cellMin = Intervals.minAsLongArray( img );
		final long[] cellMax = Intervals.maxAsLongArray( img );
		final long[] featureMin = LongStream.concat( Arrays.stream( cellMin ), LongStream.of( 0 ) ).toArray();
		final long[] featureMax = LongStream.concat( Arrays.stream( cellMax ), LongStream.of( numFeatures - 1 ) ).toArray();
		final IntervalView< T > featuresBlock = Views.interval( features, new FinalInterval( featureMin, featureMax ) );

		classifier.predictLabels( Views.collapse(featuresBlock), img );

	}
}