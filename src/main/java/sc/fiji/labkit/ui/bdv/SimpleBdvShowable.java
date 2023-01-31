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
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labkit.ui.utils.LabkitUtils;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Cast;
import net.imglib2.util.Pair;

/**
 * Implementation of {@link BdvShowable} that wraps around
 * {@link RandomAccessibleInterval}.
 */
class SimpleBdvShowable implements BdvShowable {

	private final RandomAccessibleInterval<? extends NumericType<?>> image;
	private final AffineTransform3D transformation;

	SimpleBdvShowable(RandomAccessibleInterval<? extends NumericType<?>> image,
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
	public BdvStackSource<?> show(String title, BdvOptions options) {
		Pair<Double, Double> minMax = LabkitUtils.estimateMinMax(image);
		BdvStackSource<?> source = BdvFunctions.show(Cast.unchecked(image), title, options
			.sourceTransform(transformation));
		source.setDisplayRange(minMax.getA(), minMax.getB());
		return source;
	}
}
