
package net.imglib2.labkit.inputimage;

import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imagej.ImgPlus;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Cast;

/**
 * Wrapper around {@link AbstractSpimData} that implements {@link InputImage}.
 */
public class SpimDataInputImage implements InputImage {

	private final AbstractSpimData<?> spimData;

	private final ImgPlus<? extends NumericType<?>> imageForSegmentation;

	private final String filename;

	public SpimDataInputImage(String filename, Integer level) {
		try {
			this.spimData = new XmlIoSpimDataMinimal().load(filename);
			this.imageForSegmentation = Cast.unchecked(SpimDataToImgPlus.wrap(spimData, level));
			this.filename = filename;
		}
		catch (SpimDataException e) {
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
	public String filename() {
		return filename;
	}

	@Override
	public ImgPlus<? extends NumericType<?>> imageForSegmentation() {
		return imageForSegmentation;
	}
}
