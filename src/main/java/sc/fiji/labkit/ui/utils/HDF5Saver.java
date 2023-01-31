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

package sc.fiji.labkit.ui.utils;

import bdv.export.ExportMipmapInfo;
import bdv.export.ProgressWriter;
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
import sc.fiji.labkit.pixel_classification.RevampUtils;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;
import sc.fiji.labkit.ui.utils.progress.DummyProgressWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * HDF5Saver provides a simple API to save an {@link RandomAccessibleInterval}
 * as Big Data Viewer HDF5 + XML format.
 * </p>
 * 
 * <pre>
 * HDF5Saver saver = new HDF5Saver(result, xml.getAbsolutePath());
 * saver.writeAll();
 * </pre>
 * <p>
 * There are two noteworthy features:
 * </p>
 * <p>
 * First: It's possible to set a {@link ProgressWriter} for tracking progress:
 * </p>
 * 
 * <pre>
 * saver.setProgressWriter(new ProgressWriterConsole());
 * </pre>
 * <p>
 * Second: The HDF5 file could be split into multiple partitions. This could be
 * used to have for example one HDF5 file for each timepoint. There is one way
 * to write all partitions, plus XML, plus a HDF5 file that contains a table of
 * content:
 * </p>
 *
 * <pre>
 * HDF5Saver saver = new HDF5Saver(result, xml.getAbsolutePath());
 * saver.setPartitions(1, 1);
 * saver.writeAll();
 * </pre>
 * <p>
 * And there is the option to write each partition individually. Which is useful
 * when you want to distribute the task between multiple processes. Please note
 * using multi threading want give you a speed up. As the underlying library for
 * HDF5 writing doesn't support multi threading.
 * </p>
 * 
 * <pre>
 * HDF5Saver saver = new HDF5Saver(image, xml.getAbsolutePath());
 * saver.setPartitions(1, 1);
 *
 * // This will write the XML and a small HDF5 file which is basically the table of content for the partitions
 * saver.writeXmlAndHdf5();
 *
 * // Write the partitions one by one
 * for (int i = 0; i &lt; saver.numberOfPartitions(); i++)
 * 	saver.writePartition(i);
 * </pre>
 *
 * @author Matthias Arzt
 */
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
