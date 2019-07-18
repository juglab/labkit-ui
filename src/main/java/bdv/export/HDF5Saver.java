
package bdv.export;

import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.MissingViews;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HDF5Saver {

	private ProgressWriter progressWriter = new ProgressWriterConsole();

	public void setProgressWriter(ProgressWriter progressWriter) {
		this.progressWriter = progressWriter;
	}

	public void save(String filename, RandomAccessibleInterval<?> image) {
		final String nameWithoutExtension = removeExtension(new File(filename)
			.getName(), "h5", "xml");
		final File basePath = new File(filename).getParentFile();
		final File hdf5 = new File(basePath, nameWithoutExtension + ".h5");
		final File xml = new File(basePath, nameWithoutExtension + ".xml");
		image = toUnsignedShortType(image);
		SpimDataMinimal data = oneImageSpimData(sliceTime(ensure3d(image)),
			basePath);
		writeHDF5(hdf5, data);
		setHDF5ImgLoader(hdf5, data);
		writeXML(xml, data);
	}

	private <T> RandomAccessibleInterval<T> ensure3d(
		RandomAccessibleInterval<T> image)
	{
		if (image.numDimensions() == 2) return Views.addDimension(image, 0, 0);
		return image;
	}

	private <T> List<RandomAccessibleInterval<T>> sliceTime(
		RandomAccessibleInterval<T> image)
	{
		if (image.numDimensions() == 4) return RevampUtils.slices(image);
		return Collections.singletonList(image);
	}

	private static <T> RandomAccessibleInterval<UnsignedShortType>
		toUnsignedShortType(RandomAccessibleInterval<T> image)
	{
		Object type = Util.getTypeFromInterval(image);
		if (type instanceof UnsignedShortType)
			return (RandomAccessibleInterval<UnsignedShortType>) image;
		final Converter<T, UnsignedShortType> converter =
			(Converter<T, UnsignedShortType>) getConverter(type);
		return Converters.convert(image, converter, new UnsignedShortType());
	}

	private static Converter<?, UnsignedShortType> getConverter(Object type) {
		if (type instanceof IntegerType)
			return (Converter<IntegerType<?>, UnsignedShortType>) (i, o) -> o.set(i
				.getInteger());
		if (type instanceof FloatType)
			return (Converter<FloatType, UnsignedShortType>) (i, o) -> o.set((int) (i
				.getRealFloat() * 0xffff));
		if (type instanceof DoubleType)
			return (Converter<DoubleType, UnsignedShortType>) (i, o) -> o.set((int) (i
				.getRealDouble() * 0xffff));
		if (type instanceof RealType)
			return (Converter<IntegerType<?>, UnsignedShortType>) (i, o) -> o.setReal(
				i.getRealDouble());
		throw new UnsupportedOperationException();
	}

	public static void writeXML(File xml, SpimDataMinimal data) {
		try {
			new XmlIoSpimDataMinimal().save(data, xml.getAbsolutePath());
		}
		catch (SpimDataException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setHDF5ImgLoader(File hdf5, SpimDataMinimal data) {
		BasicImgLoader imgLoader = new Hdf5ImageLoader(hdf5, null, data
			.getSequenceDescription());
		data.getSequenceDescription().setImgLoader(imgLoader);
	}

	public void writeHDF5(File hdf5, SpimDataMinimal data) {
		Map<Integer, ExportMipmapInfo> mipmapInfo = ProposeMipmaps.proposeMipmaps(
			data.getSequenceDescription());
		WriteSequenceToHdf5.writeHdf5File(data.getSequenceDescription(), mipmapInfo,
			true, hdf5, null, null, 8, progressWriter);
	}

	private static String removeExtension(String filename, String... extensions) {
		for (String extension : extensions) {
			if (filename.endsWith("." + extension)) return filename.substring(0,
				filename.length() - 1 - extension.length());
		}
		return filename;
	}

	private static <T> SpimDataMinimal oneImageSpimData(
		List<RandomAccessibleInterval<T>> frames, File basePath)
	{
		List<TimePoint> timePointList = IntStream.range(0, frames.size()).mapToObj(
			TimePoint::new).collect(Collectors.toList());
		final TimePoints timePoints = new TimePoints(timePointList);
		final BasicViewSetup setup = new BasicViewSetup(0, "image",
			(Dimensions) frames.get(0), new FinalVoxelDimensions("pixel", 1, 1, 1));
		final Map<Integer, BasicViewSetup> setups = Collections.singletonMap(0,
			setup);
		final MissingViews missingViews = null;
		BasicImgLoader imgLoader = new BasicImgLoader() {

			@Override
			public BasicSetupImgLoader<T> getSetupImgLoader(int i) {
				if (i != setup.getId()) throw new IllegalArgumentException();
				return new BasicSetupImgLoader<T>() {

					@Override
					public RandomAccessibleInterval<T> getImage(int timeId,
						ImgLoaderHint... imgLoaderHints)
					{
						return frames.get(timeId);
					}

					@Override
					public T getImageType() {
						return Util.getTypeFromInterval(frames.get(0));
					}
				};
			}
		};
		final SequenceDescriptionMinimal sequence = new SequenceDescriptionMinimal(
			timePoints, setups, imgLoader, missingViews);
		List<ViewRegistration> viewRegistrationList = timePointList.stream().map(
			x -> new ViewRegistration(x.getId(), setup.getId())).collect(Collectors
				.toList());
		ViewRegistrations registrations = new ViewRegistrations(
			viewRegistrationList);
		return new SpimDataMinimal(basePath, sequence, registrations);
	}
}
