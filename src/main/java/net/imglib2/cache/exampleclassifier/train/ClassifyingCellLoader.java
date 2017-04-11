package net.imglib2.cache.exampleclassifier.train;

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
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class ClassifyingCellLoader< T extends RealType< T > > implements CacheLoader< Long, Cell< VolatileShortArray > >
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

	public ClassifyingCellLoader(
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
		final long[] cellMax = IntStream.range( 0, n ).mapToLong( d -> cellMin[ d ] + cellDims[ d ] - 1 ).toArray();
		final FinalInterval cellInterval = new FinalInterval( cellMin, cellMax );

		final int blocksize = ( int ) Intervals.numElements( cellDims );
		final VolatileShortArray array = new VolatileShortArray( blocksize, true );

		final Img< ShortType > img = ArrayImgs.shorts( array.getCurrentStorageArray(), Util.int2long( cellDims ) );

		final InstanceView< T, ? > instances = new InstanceView<>( features, InstanceView.makeDefaultAttributes( numFeatures, numClasses ) );

		final Cursor< Instance > instancesCursor = Views.interval( instances, cellInterval ).cursor();
		final Cursor< ShortType > imgCursor = img.cursor();
		while ( imgCursor.hasNext() )
			imgCursor.next().setInteger( ( int ) classifier.classifyInstance( instancesCursor.next() ) );

		return new Cell<>( cellDims, cellMin, array );
	}
}