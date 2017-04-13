package net.imglib2.atlas.classification;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.CacheLoader;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.VolatileShortAccess;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class ClassifyingCacheLoader< T extends RealType< T >, A extends VolatileShortAccess > implements CacheLoader< Long, Cell< A > >
{

	public static interface ShortAccessGenerator< A extends VolatileShortAccess > {
		public A create( long numEntities, boolean isValid );
	}

	private final CellGrid grid;

	private final RandomAccessible< T > features;

	private Classifier< ?, RandomAccessibleInterval< T >, RandomAccessibleInterval< ShortType > > classifier;

	private final int numFeatures;

	private final ShortAccessGenerator< A > accessGenerator;

	public void setClassifier( final Classifier< ?, RandomAccessibleInterval< T >, RandomAccessibleInterval< ShortType > > classifier )
	{
		this.classifier = classifier;
	}

	public ClassifyingCacheLoader(
			final CellGrid grid,
			final RandomAccessible< T > features,
			final Classifier< ?, RandomAccessibleInterval< T >, RandomAccessibleInterval< ShortType > > classifier,
					final int numFeatures,
					final ShortAccessGenerator< A > accessGenerator )
	{
		this.grid = grid;
		this.features = features;
		this.classifier = classifier;
		this.numFeatures = numFeatures;
		this.accessGenerator = accessGenerator;
	}

	@Override
	public Cell< A > get( final Long key ) throws Exception
	{
		final long index = key;

		final int n = grid.numDimensions();
		final long[] cellMin = new long[ n ];
		final int[] cellDims = new int[ n ];
		grid.getCellDimensions( index, cellMin, cellDims );
		final long[] cellMax = IntStream.range( 0, n ).mapToLong( d -> cellMin[ d ] + cellDims[ d ] - 1 ).toArray();

		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final A store = accessGenerator.create( blocksize, true );

		final ArrayImg< ShortType, A > img = ArrayImgs.shorts( store, Util.int2long( cellDims ) );

		final long[] featureMin = LongStream.concat( Arrays.stream( cellMin ), LongStream.of( 0 ) ).toArray();
		final long[] featureMax = LongStream.concat( Arrays.stream( cellMax ), LongStream.of( numFeatures - 1 ) ).toArray();
		final IntervalView< T > featuresBlock = Views.offsetInterval( features, new FinalInterval( featureMin, featureMax ) );

		classifier.predictLabels( featuresBlock, img );

		return new Cell<>( cellDims, cellMin, store );
	}
}