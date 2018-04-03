package net.imglib2.labkit.bdv;

import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.viewer.Source;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.Objects;

public interface BdvShowable {

	static BdvShowable wrap(RandomAccessibleInterval<? extends NumericType<?>> image) {
		return wrap(image, new AffineTransform3D());
	}

	static BdvShowable wrap(RandomAccessibleInterval<? extends NumericType<?>> image, AffineTransform3D transformation)
	{
		return new SimpleBdvShowable( Objects.requireNonNull( image ), transformation );
	}

	static BdvShowable wrap(AbstractSpimData<?> spimData)
	{
		return new SpimBdvShowable( Objects.requireNonNull( spimData ) );
	}

	static BdvShowable wrap(Source<? extends NumericType<?>> source) {
		return new SourceBdvShowable( source );
	}

	Interval interval();

	AffineTransform3D transformation();

	BdvSource show(String title, BdvOptions options);
}
