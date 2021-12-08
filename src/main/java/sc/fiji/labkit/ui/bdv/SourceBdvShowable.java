
package sc.fiji.labkit.ui.bdv;

import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

/**
 * Implementation of {@link BdvShowable} that wraps around a {@link Source}.
 */
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
	public BdvStackSource<?> show(String title, BdvOptions options) {
		return BdvFunctions.show(source, options);
	}
}
