package net.imglib2.cache.exampleclassifier.train;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.PartialDerivative;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class SimpleFeatureGenerator< T extends RealType< T > > implements FeatureGenerator< T, T >
{

	private final double[][] sigmas;

	public SimpleFeatureGenerator( final double[][] sigmas )
	{
		super();
		this.sigmas = sigmas;
	}

	@Override
	public void generateFeatures( final RandomAccessible< T > source, final RandomAccessibleInterval< T > target )
	{
		final ExecutorService es = Executors.newSingleThreadExecutor();
		final int nDim = source.numDimensions();
		final int stride = nDim + 2;
		for ( int index = 0, offset = 0; index < sigmas.length; ++index, offset += stride )
		{
			final double[] sigma = sigmas[ index ];
			final IntervalView< T > gauss = Views.hyperSlice( target, nDim, offset );
			try
			{
				Gauss3.gauss( sigma, source, gauss, es );
			}
			catch ( final IncompatibleTypeException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			final IntervalView< T > gradientMagnitude = Views.hyperSlice( target, nDim, offset + 1 );

			for ( int d = 0; d < nDim; ++d )
			{
				final IntervalView< T > gradient = Views.hyperSlice( target, nDim, offset + 2 + d );
				PartialDerivative.gradientCentralDifference( Views.extendBorder( gauss ), gradient, d );
				final T dummy = gradient.firstElement().copy();
				for ( final Pair< T, T > pair : Views.interval( Views.pair( gradient, gradientMagnitude ), gradient ) )
				{
					dummy.set( pair.getA() );
					dummy.mul( dummy );
					pair.getB().add( dummy );

				}
			}
		}
	}

	@Override
	public int numFeatures( final int numDimensions )
	{
		// gauss for each sigma, gradient in each direction for each sigma, and gradient magnitude for each sigma
		return sigmas.length + numDimensions * sigmas.length + sigmas.length;
	}

}
