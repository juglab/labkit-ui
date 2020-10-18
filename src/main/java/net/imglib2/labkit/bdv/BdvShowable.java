
package net.imglib2.labkit.bdv;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import bdv.viewer.Source;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imagej.ImgPlus;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.Objects;

/**
 * A {@link BdvShowable} is "something that can be shown with
 * {@link BdvFunctions}".
 * <p>
 * (The most important method is {@link BdvShowable#show} that shows the
 * object.)
 * <p>
 * There are actually at least three different classes that can be shown with
 * {@link BdvFunctions}.
 * <ul>
 * <li>{@link RandomAccessibleInterval}</li>
 * <li>{@link AbstractSpimData}</li>
 * <li>{@link Source}</li>
 * </ul>
 * These classes have different features, methods, and they lack a common
 * interface. This causes a problem for Labkit where we currently want to
 * support all these three classes. (Until there is a better solution.) There is
 * no type that can be used to represent all different objects that can be shown
 * with BdvFunctions. The solutions is to have the BdvShowable interface, and
 * wrappers
 * <ul>
 * <li>{@link ImgPlusBdvShowable}</li>
 * <li>{@link SpimBdvShowable}</li>
 * <li>{@link SourceBdvShowable}</li>
 * </ul>
 * that implement BdvShowable, and wrap arround the classes mentioned above.
 *
 * @author Matthias Arzt
 */
public interface BdvShowable {

	static BdvShowable wrap(
		RandomAccessibleInterval<? extends NumericType<?>> image)
	{
		return wrap(image, new AffineTransform3D());
	}

	static BdvShowable wrap(
		RandomAccessibleInterval<? extends NumericType<?>> image,
		AffineTransform3D transformation)
	{
		return new SimpleBdvShowable(Objects.requireNonNull(image), transformation);
	}

	static BdvShowable wrap(ImgPlus<? extends NumericType<?>> image) {
		return new ImgPlusBdvShowable(image);
	}

	static BdvShowable wrap(AbstractSpimData<?> spimData) {
		return new SpimBdvShowable(Objects.requireNonNull(spimData));
	}

	static BdvShowable wrap(Source<? extends NumericType<?>> source) {
		return new SourceBdvShowable(source);
	}

	Interval interval();

	AffineTransform3D transformation();

	BdvStackSource<?> show(String title, BdvOptions options);
}
