package net.imglib2.cache.exampleclassifier.train;

import java.util.Arrays;
import java.util.stream.IntStream;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.cache.CacheLoader;
import net.imglib2.cache.exampleclassifier.InstanceView;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.img.cell.Cell;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.RealComposite;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class ProbabilityMapLoader< T extends RealType< T > > implements CacheLoader< Long, Cell< VolatileShortArray > >
{
	private final CellGrid grid;

	private final RandomAccessible< ? extends Composite< T > > features;

	private Classifier classifier;

	private final int numFeatures;

	private final int numClasses;

	public void setClassifier( final Classifier classifier )
	{
		this.classifier = classifier;
	}

	public ProbabilityMapLoader(
			final CellGrid grid,
			final RandomAccessible< ? extends Composite< T > > features,
					final Classifier classifier,
					final int numFeatures,
					final int numClasses )
	{
		this.grid = grid;
		this.features = features;
		this.classifier = classifier;
		this.numFeatures = numFeatures;
		this.numClasses = numClasses;
	}

	@Override
	public Cell< VolatileShortArray > get( final Long key ) throws Exception
	{
		final long index = key;

		final int n = grid.numDimensions();
		final long[] cellMin = new long[ n ];
		final int[] cellDims = new int[ n ];
		grid.getCellDimensions( index, cellMin, cellDims );

		assert cellDims[ cellDims.length - 1 ] == this.numClasses;

		final long[] cellMax = IntStream.range( 0, n ).mapToLong( d -> cellMin[ d ] + cellDims[ d ] - 1 ).toArray();

		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final VolatileShortArray array = new VolatileShortArray( blocksize, true );

		final Img< UnsignedShortType > img = ArrayImgs.unsignedShorts( array.getCurrentStorageArray(), Util.int2long( cellDims ) );

		final InstanceView< T, ? > instances = new InstanceView<>( features, InstanceView.makeDefaultAttributes( numFeatures, numClasses ) );

		final long[] sourceMin = Arrays.stream( cellMin ).limit( cellMin.length - 1 ).toArray();
		final long[] sourceMax = Arrays.stream( cellMax ).limit( cellMax.length - 1 ).toArray();
		final FinalInterval sourceCellInterval = new FinalInterval( sourceMin, sourceMax );
		final Cursor< Instance > instancesCursor = Views.flatIterable( Views.interval( instances, sourceCellInterval ) ).cursor();
		final Cursor< RealComposite< UnsignedShortType > > imgCursor = Views.flatIterable( Views.interval( Views.collapseReal( img ), img ) ).cursor();
		while ( imgCursor.hasNext() )
		{
			final double[] pred = classifier.distributionForInstance( instancesCursor.next() );
			final RealComposite< UnsignedShortType > target = imgCursor.next();
			for ( int d = 0; d < pred.length; ++d )
				target.get( d ).setReal( pred[ d ] );
		}

		return new Cell<>( cellDims, cellMin, array );
	}
}