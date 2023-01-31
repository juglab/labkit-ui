/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.bdv;

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
