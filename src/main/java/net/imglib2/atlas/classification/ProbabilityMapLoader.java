package net.imglib2.atlas.classification;

import java.util.Arrays;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.features.InstanceView;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.imglib2.view.composite.Composite;
import net.imglib2.view.composite.RealComposite;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class ProbabilityMapLoader< T extends RealType< T > > implements CellLoader< UnsignedShortType >
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
	public void load( final Img< UnsignedShortType > img ) throws Exception
	{

		final long[] cellMin = Intervals.minAsLongArray( img );
		final long[] cellMax = Intervals.maxAsLongArray( img );
		final InstanceView instances = getInstanceView(features);

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

	}

	private InstanceView getInstanceView(RandomAccessible<? extends Composite<? extends RealType<?>>> features) {
		return new InstanceView<>( features, InstanceView.makeDefaultAttributes( numFeatures, numClasses ) );
	}
}