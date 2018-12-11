
package net.imglib2.labkit.bdv;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.labkit.utils.Casts;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Pair;

class SimpleBdvShowable implements BdvShowable {

	private final RandomAccessibleInterval<?> image;
	private final AffineTransform3D transformation;

	SimpleBdvShowable(RandomAccessibleInterval<?> image,
		AffineTransform3D transformation)
	{
		this.image = image;
		this.transformation = transformation;
	}

	@Override
	public Interval interval() {
		return new FinalInterval(image);
	}

	@Override
	public AffineTransform3D transformation() {
		return transformation;
	}

	@Override
	public BdvSource show(String title, BdvOptions options) {
		Pair<Double, Double> minMax = LabkitUtils.estimateMinMax(image);
		BdvSource source = BdvFunctions.show(Casts.unchecked(image), title, options
			.sourceTransform(transformation));
		source.setDisplayRange(minMax.getA(), minMax.getB());
		return source;
	}
}
