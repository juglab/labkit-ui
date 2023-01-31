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

package sc.fiji.labkit.ui.utils.sparse;

import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.VirtualStackAdapter;
import sc.fiji.labkit.ui.utils.HDF5Saver;
import sc.fiji.labkit.ui.utils.progress.SwingProgressWriter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.io.File;
import java.io.IOException;

public class HDF5SaverDemo {

	public static <T extends RealType<T>> void main(String... args)
		throws SpimDataException, IOException
	{
		String outputFilename = File.createTempFile("output-", ".xml")
			.getAbsolutePath();
		RandomAccessibleInterval<T> image =
			(RandomAccessibleInterval<T>) VirtualStackAdapter.wrap(new ImagePlus(
				"https://imagej.nih.gov/ij/images/t1-head.zip"));
		image = Views.interval(Views.extendPeriodic(image), new FinalInterval(1000,
			1000, 1000));
		RandomAccessibleInterval<UnsignedShortType> result = treshold(image);
		HDF5Saver saver = new HDF5Saver(result, outputFilename);
		saver.setProgressWriter(new SwingProgressWriter(null, "Save Huge Image"));
		saver.writeAll();
	}

	public static RandomAccessibleInterval<UnsignedShortType> treshold(
		RandomAccessibleInterval<? extends RealType<?>> image)
	{
		return Converters.convert(image, (i, o) -> o.set(i.getRealDouble() > 20 ? 1
			: 0), new UnsignedShortType());
	}
}
