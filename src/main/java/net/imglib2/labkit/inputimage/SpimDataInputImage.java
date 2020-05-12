
package net.imglib2.labkit.inputimage;

import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.imagej.ImgPlus;
import net.imglib2.Dimensions;
import net.imglib2.labkit.bdv.BdvShowable;
import net.imglib2.labkit.utils.Casts;
import net.imglib2.labkit.utils.CheckedExceptionUtils;
import net.imglib2.labkit.utils.LabkitUtils;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;

import java.util.Arrays;
import java.util.stream.IntStream;

public class SpimDataInputImage implements InputImage {

	private final AbstractSpimData<?> spimData;

	private final ImgPlus<? extends NumericType<?>> imageForSegmentation;

	private final String defaultLabelingFilename;

	public SpimDataInputImage(String filename, Integer level) {
		this.spimData = CheckedExceptionUtils.run(() -> new XmlIoSpimDataMinimal().load(filename));
		this.imageForSegmentation = Casts.unchecked(SpimDataToImgPlus.wrap(spimData, level));
		this.defaultLabelingFilename = filename + ".labeling";
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
