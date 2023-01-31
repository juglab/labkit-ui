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

package sc.fiji.labkit.ui.inputimage;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imglib2.img.display.imagej.ImgPlusViews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Class for {@link ImgPlus} related utility functions.
 */
// TODO: make this avaiblable in imglib2
public class ImgPlusViewsOld {

	public static <T> ImgPlus<T> sortAxes(ImgPlus<T> in,
		final List<AxisType> order)
	{
		if (in.numDimensions() == 0) return in;
		ImgPlus<T> out = in;
		boolean changend = true;
		while (changend) {
			changend = false;
			for (int i = 0; i < out.numDimensions() - 1; i++) {
				if (order.indexOf(out.axis(i).type()) > order.indexOf(out.axis(i + 1)
					.type()))
				{
					out = ImgPlusViews.permute((ImgPlus) out, i, i + 1);
					changend = true;
				}
			}
		}
		if (out != in)
			copyMetadataFromTo(in, out);
		return out;
	}

	/**
	 * Change the axis types of an image, such that each axis is uniquely typed as
	 * X, Y, Z, channel or time. Existing unique axis of type: X, Y, Z, channel or
	 * time are preserved.
	 */
	public static <T> ImgPlus<T> fixAxes(final ImgPlus<T> in,
		final List<AxisType> allowed)
	{
		final List<AxisType> newAxisTypes = fixAxes(getAxes(in), allowed);
		final CalibratedAxis[] newAxes = IntStream.range(0, in.numDimensions())
			.mapToObj(i -> {
				final CalibratedAxis newAxis = in.axis(i).copy();
				newAxis.setType(newAxisTypes.get(i));
				return newAxis;
			}).toArray(CalibratedAxis[]::new);
		ImgPlus<T> out = new ImgPlus<>(in.getImg(), in.getName(), newAxes);
		copyMetadataFromTo(in, out);
		return out;
	}

	private static <T> void copyMetadataFromTo(ImgPlus<T> in, ImgPlus<T> out) {
		out.setCompositeChannelCount(in.getCompositeChannelCount());
		int d = in.dimensionIndex(Axes.CHANNEL);
		long channelCount = d < 0 ? 1 : in.dimension(d);
		for (int i = 0; i < channelCount; i++) {
			out.setChannelMinimum(i, in.getChannelMinimum(i));
			out.setChannelMaximum(i, in.getChannelMaximum(i));
		}
		out.initializeColorTables(in.getColorTableCount());
		for (int i = 0; i < in.getColorTableCount(); i++)
			out.setColorTable(in.getColorTable(i), i);
	}

	// -- Helper methods --

	private static List<AxisType> fixAxes(final List<AxisType> in,
		final List<AxisType> allowed)
	{
		final List<AxisType> unusedAxis = new ArrayList<>(allowed);
		unusedAxis.removeAll(in);
		final Predicate<AxisType> isDuplicate = createIsDuplicatePredicate();
		final Predicate<AxisType> replaceIf = axis -> isDuplicate.test(axis) ||
			!allowed.contains(axis);
		final Iterator<AxisType> iterator = unusedAxis.iterator();
		final Supplier<AxisType> replacements = () -> iterator.hasNext() ? iterator
			.next() : Axes.unknown();
		return replaceMatches(in, replaceIf, replacements);
	}

	// NB: Package-private to allow tests.
	public static List<AxisType> getAxes(final ImgPlus<?> in) {
		return IntStream.range(0, in.numDimensions()).mapToObj(in::axis).map(
			CalibratedAxis::type).collect(Collectors.toList());
	}

	// NB: Package-private to allow tests.
	private static <T> Predicate<T> createIsDuplicatePredicate() {
		final Set<T> before = new HashSet<>();
		return element -> {
			final boolean isDuplicate = before.contains(element);
			if (!isDuplicate) before.add(element);
			return isDuplicate;
		};
	}

	// NB: Package-private to allow tests.
	private static <T> List<T> replaceMatches(final List<T> in,
		final Predicate<T> predicate, final Supplier<T> replacements)
	{
		return in.stream().map(value -> predicate.test(value) ? replacements.get()
			: value).collect(Collectors.toList());
	}

	public static boolean hasAxis(ImgPlus<?> image, AxisType axes) {
		return image.dimensionIndex(axes) >= 0;
	}

	public static List<CalibratedAxis> getCalibratedAxes(ImgPlus<?> image) {
		CalibratedAxis[] axes = new CalibratedAxis[image.numDimensions()];
		image.axes(axes);
		return Arrays.asList(axes);
	}

	public static long getDimension(ImgPlus<?> image, AxisType axis) {
		return image.dimension(image.dimensionIndex(axis));
	}

	public static int numberOfSpatialDimensions(ImgPlus<?> imgPlus) {
		int n = 0;
		for (AxisType axes : getAxes(imgPlus))
			if (axes.isSpatial())
				n++;
		return n;
	}

	public static <T> ImgPlus<T> hyperSlice(ImgPlus<T> image, AxisType axis, long position) {
		int d = image.dimensionIndex(axis);
		if (d < 0)
			return image;
		return ImgPlusViews.hyperSlice((ImgPlus) image, d, position);
	}

	public static <T> List<ImgPlus<?>> hyperSlices(ImgPlus<T> image, AxisType axis) {
		int d = image.dimensionIndex(axis);
		if (d < 0)
			return Collections.singletonList(image);
		return LongStream.rangeClosed(image.min(d), image.max(d)).mapToObj(position -> hyperSlice(image,
			axis, position)).collect(Collectors.toList());
	}

}
