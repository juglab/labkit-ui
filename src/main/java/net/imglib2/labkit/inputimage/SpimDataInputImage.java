
package net.imglib2.labkit.inputimage;

import bdv.img.imaris.Imaris;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imagej.ImgPlus;
import net.imglib2.labkit.bdv.BdvShowable;
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
