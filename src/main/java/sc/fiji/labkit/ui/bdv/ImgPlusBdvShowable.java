/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2021 Matthias Arzt
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

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.NumericType;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of {@link BdvShowable} that wraps around {@link ImgPlus}.
 */
class ImgPlusBdvShowable implements BdvShowable {

	private final ImgPlus<? extends NumericType<?>> image;

	ImgPlusBdvShowable(ImgPlus<? extends NumericType<?>> image) {
		this.image = image;
	}

	@Override
	public Interval interval() {
		return image;
	}

	@Override
	public AffineTransform3D transformation() {
		AffineTransform3D transform = new AffineTransform3D();
		transform.set(
			getCalibration(Axes.X), 0, 0, 0,
			0, getCalibration(Axes.Y), 0, 0,
			0, 0, getCalibration(Axes.Z), 0);
		return transform;
	}

	@Override
	public BdvStackSource<?> show(String title, BdvOptions options) {
		String name = image.getName();
		BdvOptions options1 = options.axisOrder(getAxisOrder()).sourceTransform(transformation());
		return BdvFunctions.show(image, name == null ? title : name, options1);
	}

	private AxisOrder getAxisOrder() {
		String code = IntStream.range(0, image.numDimensions()).mapToObj(i -> image
			.axis(i).type().getLabel().substring(0, 1)).collect(Collectors.joining());
		try {
			return AxisOrder.valueOf(code);
		}
		catch (IllegalArgumentException e) {
			return AxisOrder.DEFAULT;
		}
	}

	private double getCalibration(AxisType axisType) {
		int d = image.dimensionIndex(axisType);
		if (d == -1) return 1;
		return image.axis(d).averageScale(image.min(d), image.max(d));
	}
}
