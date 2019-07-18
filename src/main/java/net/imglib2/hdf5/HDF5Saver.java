
package net.imglib2.hdf5;

import bdv.export.ExportMipmapInfo;
import bdv.export.ProgressWriter;
import bdv.export.ProgressWriterConsole;
import bdv.export.ProposeMipmaps;
import bdv.export.SubTaskProgressWriter;
import bdv.export.WriteSequenceToHdf5;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.Partition;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HDF5Saver {

	private ProgressWriter progressWriter = new DummyProgressWriter();
	private final File hdf5;
	private final File xml;
	private final SpimDataMinimal data;
	private ArrayList<Partition> partitions = null;
	private Map<Integer, ExportMipmapInfo> mipmapInfo;

	public HDF5Saver(RandomAccessibleInterval<?> image, String filename) {
		final File file = new File(filename);
		hdf5 = replaceExtension(file, "h5");
		xml = replaceExtension(file, "xml");
		data = wrapAsSpimData(image, xml.getParentFile());
		mipmapInfo = ProposeMipmaps.proposeMipmaps(data.getSequenceDescription());
	}

	public void setPartitions(int timepointsPerPartition, int setupsPerPartion) {
		List<TimePoint> timePoints = data.getSequenceDescription().getTimePoints()
			.getTimePointsOrdered();
		List<BasicViewSetup> setups = data.getSequenceDescription()
			.getViewSetupsOrdered();
		String basename = removeExtension(hdf5.getAbsolutePath(), "h5");
		partitions = Partition.split(timePoints, setups, timepointsPerPartition,
			setupsPerPartion, basename);
	}

	public void setProgressWriter(ProgressWriter progressWriter) {
		this.progressWriter = progressWriter;
	}

	public void writeAll() {
		writeAllPartitions();
		writeXmlAndHdf5();
	}

	public void writeXmlAndHdf5() {
		writeHdf5();
		writeXml();
	}

	private void writeHdf5() {
		if (partitions == null) writeHDF5Block();
		else writeHDF5Partitioned();
	}

	private void writeXml() {
		try {
			BasicImgLoader imgLoader = new Hdf5ImageLoader(hdf5, partitions, data
				.getSequenceDescription());
			SpimDataMinimal spimData = new SpimDataMinimal(data, imgLoader);
			new XmlIoSpimDataMinimal().save(spimData, xml.getAbsolutePath());
		}
		catch (SpimDataException e) {
			throw new RuntimeException(e);
		}
	}

	private File replaceExtension(File file, String newExtension) {
		final String nameWithoutExtension = removeExtension(file.getName(), "h5",
			"xml");
		return new File(file.getParentFile(), nameWithoutExtension + "." +
			newExtension);
	}

	private SpimDataMinimal wrapAsSpimData(RandomAccessibleInterval<?> image,
		File basePath)
	{
		return oneImageSpimData(sliceTime(ensure3d(toUnsignedShortType(image))),
			basePath);
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

	private void writeHDF5Block() {
		Map<Integer, ExportMipmapInfo> mipmapInfo = ProposeMipmaps.proposeMipmaps(
			data.getSequenceDescription());
		WriteSequenceToHdf5.writeHdf5File(data.getSequenceDescription(), mipmapInfo,
			true, hdf5, null, null, 8, progressWriter);
	}

	private void writeHDF5Partitioned() {
		SequenceDescriptionMinimal sequenceDescription = data
			.getSequenceDescription();
		WriteSequenceToHdf5.writeHdf5PartitionLinkFile(sequenceDescription,
			mipmapInfo, partitions, hdf5);
	}

	public void writeAllPartitions() {
		if (partitions == null) return;
		int size = numberOfPartitions();
		for (int i = 0; i < size; ++i) {
			double start = 0.95 * i / size;
			double end = 0.95 * (i + 1) / size;
			final ProgressWriter p = new SubTaskProgressWriter(progressWriter, start,
				end);
			writePartition(i, p);
		}
	}

	public int numberOfPartitions() {
		return partitions == null ? 0 : partitions.size();
	}

	public void writePartition(int index) {
		writePartition(index, progressWriter);
	}

	private void writePartition(int index, ProgressWriter progressWriter) {
		WriteSequenceToHdf5.writeHdf5PartitionFile(data.getSequenceDescription(),
			mipmapInfo, true, partitions.get(index), null, null, 8, progressWriter);
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
