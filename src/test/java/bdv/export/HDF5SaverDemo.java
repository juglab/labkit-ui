
package bdv.export;

import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.labkit.utils.progress.SwingProgressWriter;
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
