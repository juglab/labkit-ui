
package bdv.export;

import bdv.ViewerSetupImgLoader;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.util.BdvFunctions;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.TimePoint;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.Views;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HDF5SaverDemo {

	public static <T extends RealType<T>> void main(String... args)
		throws SpimDataException, IOException
	{
		String outputFilename = File.createTempFile("output-", ".xml")
			.getAbsolutePath();
		// TODO: cancellation doesn't work properly. The jvm stays alive after the
		// cancel button is clicked.
		RandomAccessibleInterval<T> image =
			(RandomAccessibleInterval<T>) VirtualStackAdapter.wrap(new ImagePlus(
				"https://imagej.nih.gov/ij/images/t1-head.gif"));
		image = Views.concatenate(0, image, image, image, image);
		image = Views.concatenate(1, image, image, image, image);
		RandomAccessibleInterval<UnsignedShortType> result = treshold(image);
		HDF5Saver saver = new HDF5Saver();
		saver.setProgressWriter(new SwingProgressWriter(null, "Save Huge Image"));
		saver.save(outputFilename, result);
	}

	public static RandomAccessibleInterval<UnsignedShortType> treshold(
		RandomAccessibleInterval<? extends RealType<?>> image)
	{
		return Converters.convert(image, (i, o) -> o.set(i.getRealDouble() > 20 ? 1
			: 0), new UnsignedShortType());
	}
}
