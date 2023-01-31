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

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imagej.ImgPlus;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Cast;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

/**
 * Wrapper around {@link AbstractSpimData} that implements {@link InputImage}.
 */
public class SpimDataInputImage implements InputImage {

	private final AbstractSpimData<?> spimData;

	private final ImgPlus<? extends NumericType<?>> imageForSegmentation;

	private final String defaultLabelingFilename;

	public SpimDataInputImage(String filename, Integer level) {
		this.spimData = openSpimData(filename);
		this.imageForSegmentation = Cast.unchecked(SpimDataToImgPlus.wrap(spimData, level));
		imageForSegmentation.setName(FilenameUtils.getName(filename));
		this.defaultLabelingFilename = filename + ".labeling";
	}

	private SpimDataMinimal openSpimData(String filename) {
		try {
			if (filename.endsWith(".ims"))
				return Imaris.openIms(filename);
			return new XmlIoSpimDataMinimal().load(filename);
		}
		catch (SpimDataException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static SpimDataInputImage openWithGuiForLevelSelection(
		String filename)
	{
		return new SpimDataInputImage(filename, null);
	}

	@Override
	public BdvShowable showable() {
		return BdvShowable.wrap(spimData);
	}

	@Override
	public ImgPlus<? extends NumericType<?>> imageForSegmentation() {
		return imageForSegmentation;
	}

	@Override
	public String getDefaultLabelingFilename() {
		return defaultLabelingFilename;
	}
}
