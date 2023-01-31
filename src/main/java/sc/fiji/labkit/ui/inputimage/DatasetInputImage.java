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

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImgPlusViews;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Cast;

import java.util.Arrays;
import java.util.List;

/**
 * Adapter from {@link ImgPlus} to {@link InputImage}.
 */
public class DatasetInputImage implements InputImage {

	private final ImgPlus<? extends NumericType<?>> image;
	private final BdvShowable showable;
	private String defaultLabelingFilename;

	public DatasetInputImage(ImgPlus<? extends NumericType<?>> image,
		BdvShowable showable)
	{
		this.showable = showable;
		this.image = prepareImage(image);
		this.defaultLabelingFilename = image.getSource() + ".labeling";
	}

	public DatasetInputImage(Img<?> image) {
		this(ImgPlus.wrap(image));
	}

	private static ImgPlus<? extends NumericType<?>> prepareImage(
		ImgPlus<? extends NumericType<?>> image)
	{
		List<AxisType> order = Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.CHANNEL,
			Axes.TIME);
		return ImgPlusViewsOld.sortAxes(labelAxes(image), order);
	}

	private static ImgPlus<? extends NumericType<?>> labelAxes(
		ImgPlus<? extends NumericType<?>> image)
	{
		if (image.firstElement() instanceof ARGBType) return ImgPlusViewsOld
			.fixAxes(image, Arrays.asList(Axes.X, Axes.Y, Axes.Z, Axes.TIME));
		if (image.numDimensions() == 4) return ImgPlusViewsOld.fixAxes(image, Arrays
			.asList(Axes.X, Axes.Y, Axes.Z, Axes.TIME, Axes.CHANNEL));
		return ImgPlusViewsOld.fixAxes(image, Arrays.asList(Axes.X, Axes.Y, Axes.Z,
			Axes.CHANNEL, Axes.TIME));
	}

	public DatasetInputImage(ImgPlus<?> image) {
		this(Cast.unchecked(image), initializeShowable(Cast.unchecked(image)));
	}

	public static BdvShowable initializeShowable(
		ImgPlus<? extends NumericType<?>> image)
	{
		return BdvShowable.wrap(prepareImage(image));
	}

	public DatasetInputImage(Dataset image) {
		this(image.getImgPlus());
	}

	@Override
	public BdvShowable showable() {
		return showable;
	}

	@Override
	public ImgPlus<? extends NumericType<?>> imageForSegmentation() {
		return image;
	}

	public void setDefaultLabelingFilename(String defaultLabelingFilename) {
		this.defaultLabelingFilename = defaultLabelingFilename;
	}

	@Override
	public String getDefaultLabelingFilename() {
		return defaultLabelingFilename;
	}

}
