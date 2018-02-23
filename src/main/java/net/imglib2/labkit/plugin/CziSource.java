package net.imglib2.labkit.plugin;

import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import java.util.List;

public class CziSource< T extends NumericType< T > > implements Source<T>
{
	private final List< RandomAccessibleInterval< T > > sources;

	private final T type;

	public CziSource( List< RandomAccessibleInterval< T > > sources, T type )
	{
		this.sources = sources;
		this.type = type;
	}

	@Override public boolean isPresent( int t )
	{
		return true;
	}

	@Override public RandomAccessibleInterval< T > getSource( int t, int level )
	{
		return sources.get(level);
	}

	@Override public RealRandomAccessible< T > getInterpolatedSource( int t, int level, Interpolation method )
	{
		RandomAccessible< T > source = Views.extendZero(getSource( t, level ));
		return interpolate( source );
	}

	private RealRandomAccessible< T > interpolate( RandomAccessible< T > source )
	{
		NearestNeighborInterpolatorFactory< T > factory = new NearestNeighborInterpolatorFactory<>();
		return new RealRandomAccessible< T >()
		{
			@Override public RealRandomAccess< T > realRandomAccess()
			{
				return factory.create( source );
			}

			@Override public RealRandomAccess< T > realRandomAccess( RealInterval interval )
			{
				return factory.create( source, interval );
			}

			@Override public int numDimensions()
			{
				return source.numDimensions();
			}
		};
	}

	@Override public void getSourceTransform( int t, int level, AffineTransform3D transform )
	{
		transform.identity();
	}

	@Override public T getType()
	{
		return type;
	}

	@Override public String getName()
	{
		return "sourcename";
	}

	@Override public VoxelDimensions getVoxelDimensions()
	{
		return null;
	}

	@Override public int getNumMipmapLevels()
	{
		return 1;
	}
}
