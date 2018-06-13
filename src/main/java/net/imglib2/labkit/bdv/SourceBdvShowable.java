
package net.imglib2.labkit.bdv;

import bdv.viewer.Source;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

class SourceBdvShowable implements BdvShowable {

	private final Source<? extends NumericType<?>> source;

	SourceBdvShowable(Source<? extends NumericType<?>> source) {
		this.source = source;
	}

	@Override
	public Interval interval() {
		return new FinalInterval(source.getSource(0, 0));
	}

	@Override
	public AffineTransform3D transformation() {
		AffineTransform3D transformation = new AffineTransform3D();
		source.getSourceTransform(0, 0, transformation);
		return transformation;
	}

	@Override
	public BdvSource show(String title, BdvOptions options) {
		return BdvFunctions.show(source, options);
	}
}
