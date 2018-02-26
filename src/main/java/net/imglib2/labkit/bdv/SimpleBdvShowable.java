package net.imglib2.labkit.bdv;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Pair;

public class SimpleBdvShowable implements BdvShowable {

	private final RandomAccessibleInterval< ? extends NumericType<?> > image;

	public SimpleBdvShowable(RandomAccessibleInterval<? extends NumericType<?>> image) {
		this.image = image;
	}

	@Override
	public Interval interval() {
		return new FinalInterval(image);
	}

	@Override
	public AffineTransform3D transformation() {
		return new AffineTransform3D();
	}

	@Override
	public BdvSource show(String title, BdvOptions options) {
		Pair< Double, Double > minMax = LabkitUtils.estimateMinMax(image);
		BdvSource source = BdvFunctions.show( RevampUtils.uncheckedCast(image), title, options );
		source.setDisplayRange( minMax.getA(), minMax.getB() );
		return source;
	}
}
