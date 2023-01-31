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

package sc.fiji.labkit.ui.plugin;

import bdv.util.AbstractSource;
import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.in.JPEGReader;
import loci.formats.in.ZeissCZIReader;
import loci.formats.meta.IMetadata;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CellLoader;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImgFactory;
import sc.fiji.labkit.ui.bdv.BdvShowable;
import sc.fiji.labkit.ui.inputimage.DatasetInputImage;
import sc.fiji.labkit.ui.plugin.ui.ImageSelectionDialog;
import sc.fiji.labkit.ui.utils.ParallelUtils;
import bdv.export.ProgressWriter;
import net.imglib2.parallel.Parallelization;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.xml.model.primitives.PositiveInteger;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CziOpener {

	private ProgressWriter progressWriter;

	public CziOpener(ProgressWriter progressWriter) {
		this.progressWriter = progressWriter;
	}

	public DatasetInputImage openWithDialog(String filename) {
		ImageSelectionDialog dialog = ImageSelectionDialog.show(initReader(
			filename));
		List<Integer> selectedSectionIndices = dialog.getSelectedSectionIndices();
		String labelingFilename = dialog.getLabelingFilename();
		final OptionalInt series = selectSectionResolution(filename,
			selectedSectionIndices);
		if (series.isPresent()) return openInputImage(filename, labelingFilename,
			selectedSectionIndices.get(0), series.getAsInt());
		return openResolutionPyramid(filename, labelingFilename,
			selectedSectionIndices);
	}

	private static DatasetInputImage openResolutionPyramid(String filename,
		String labelingFilename, List<Integer> selectedSectionIndices)
	{
		int fullres = selectedSectionIndices.get(0);
		List<ImgPlus<ARGBType>> pyramid = selectedSectionIndices.stream().map(
			series -> openCachedImage(filename, fullres, series)).collect(Collectors
				.toList());
		AbstractSource<ARGBType> source = new ResolutionPyramidSource<>(pyramid,
			new ARGBType(), "source");
		BdvShowable showable = BdvShowable.wrap(source);
		ImgPlus<? extends NumericType<?>> imageForSegmentation = pyramid.get(2);
		imageForSegmentation.setSource(filename);
		DatasetInputImage result = new DatasetInputImage(imageForSegmentation,
			showable);
		result.setDefaultLabelingFilename(labelingFilename);
		return result;
	}

	private DatasetInputImage openInputImage(String filename,
		String labelingFilename, int fullres, int series)
	{
		ImgPlus<ARGBType> image = Parallelization.runMultiThreaded(() -> openImage(filename, fullres,
			series));
		DatasetInputImage result = new DatasetInputImage(image);
		result.setDefaultLabelingFilename(labelingFilename);
		return result;
	}

	private ImgPlus<ARGBType> openImage(String filename, int fullres,
		int series)
	{
		MyReader reader = new MyReader(filename);
		long[] dimensions = reader.getImgDimensions(series);
		int[] cellDimensions = reader.getCellDimensions(series);
		Img<ARGBType> out = new CellImgFactory<>(new ARGBType(), cellDimensions)
			.create(dimensions);
		Consumer<RandomAccessibleInterval<ARGBType>> operation = cell -> reader.readToInterval(series,
			cell);
		ProgressWriter progressWriter = this.progressWriter;
		ParallelUtils.applyOperationOnCells(out, cellDimensions, operation, progressWriter);
		return imgPlus(filename, out, reader.getCalibratedAxes(fullres, series));
	}

	public static ImgPlus<ARGBType> openCachedImage(String filename, int fullres,
		int series)
	{
		MyReader reader = new MyReader(filename);
		long[] dimensions = reader.getImgDimensions(series);
		int[] cellDimensions = reader.getCellDimensions(series);
		Img<ARGBType> image = setupCachedImage(cell -> reader.readToInterval(series,
			cell), new CellGrid(dimensions, cellDimensions), new ARGBType());
		return imgPlus(filename, image, reader.getCalibratedAxes(fullres, series));
	}

	private static <T extends NativeType<T>> Img<T> setupCachedImage(
		CellLoader<T> loader, CellGrid grid, T type)
	{
		DiskCachedCellImgOptions optional = DiskCachedCellImgOptions.options()
			.cellDimensions(getCellDimensions(grid)).cacheType(
				DiskCachedCellImgOptions.CacheType.SOFTREF);
		final DiskCachedCellImgFactory<T> factory = new DiskCachedCellImgFactory<>(
			type, optional);
		return factory.create(grid.getImgDimensions(), loader);
	}

	private static int[] getCellDimensions(CellGrid grid) {
		int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}

	private static ImgPlus<ARGBType> imgPlus(String filename, Img<ARGBType> out,
		CalibratedAxis[] axis)
	{
		ImgPlus<ARGBType> imgPlus = new ImgPlus<>(out, filename, axis);
		imgPlus.setSource(filename);
		return imgPlus;
	}

	private static OptionalInt selectSectionResolution(String filename,
		List<Integer> sectionIds)
	{
		List<String> list = listResolutions(filename, sectionIds);
		String pyramid = "Resolution Pyramid";
		Object[] options = Stream.concat(Stream.of(pyramid), list.stream())
			.toArray();
		Object result = JOptionPane.showInputDialog(null, "Select Image Resolution",
			"Labkit - Import Image", JOptionPane.PLAIN_MESSAGE, null, options, list
				.get(0));
		if (result == null) throw new CancellationException();
		if (result.equals(pyramid)) return OptionalInt.empty();
		return OptionalInt.of(sectionIds.get(list.indexOf(result)));
	}

	private static List<String> listResolutions(String filename,
		List<Integer> sectionIds)
	{
		ImageReader reader = initReader(filename);
		return sectionIds.stream().map(series -> {
			reader.setSeries(series);
			return reader.getSizeX() + " x " + reader.getSizeY();
		}).collect(Collectors.toList());
	}

	// -- Helper methods --

	private static ImageReader initReader(String filename) {
		ImageReader reader = initReader();
		readerSetFile(reader, filename);
		return reader;
	}

	private static ValuePair<ImageReader, IMetadata> initReaderAndMetaData(
		String filename)
	{
		ImageReader reader = initReader();
		IMetadata metadata = MetadataTools.createOMEXMLMetadata();
		reader.setMetadataStore(metadata);
		readerSetFile(reader, filename);
		return new ValuePair<>(reader, metadata);
	}

	private static void readerSetFile(ImageReader reader, String filename) {
		try {
			reader.setId(filename);
		}
		catch (FormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ImageReader initReader() {
		ClassList<IFormatReader> cl = new ClassList<>(IFormatReader.class);
		cl.addClass(JPEGReader.class);
		cl.addClass(ZeissCZIReader.class);
		return new ImageReader(cl);
	}

	// -- Helper classes --

	private static class MyReader {

		private final ImageReader reader;

		private final IMetadata metadata;

		public MyReader(String filename) {
			ValuePair<ImageReader, IMetadata> readerAndMetaData =
				initReaderAndMetaData(filename);
			reader = readerAndMetaData.getA();
			metadata = readerAndMetaData.getB();
		}

		private ImageReader getReader(int series) {
			ImageReader reader = this.reader;
			reader.setSeries(series);
			return reader;
		}

		private long[] getImgDimensions(int series) {
			ImageReader reader = getReader(series);
			return new long[] { reader.getSizeX(), reader.getSizeY() };
		}

		private int[] getCellDimensions(int series) {
			ImageReader reader = getReader(series);
			return new int[] { reader.getOptimalTileWidth(), reader
				.getOptimalTileHeight() };
		}

		private void readToInterval(int series,
			RandomAccessibleInterval<ARGBType> interval)
		{
			try {
				int[] min = Intervals.minAsIntArray(interval);
				int[] size = Intervals.dimensionsAsIntArray(interval);
				ImageReader reader = getReader(series);
				byte[] bytes = reader.openBytes(0, min[0], min[1], size[0], size[1]);
				Cursor<ARGBType> cursor = Views.flatIterable(interval).cursor();
				int index = 0;
				while (cursor.hasNext()) {
					int color = ARGBType.rgba(bytes[index], bytes[index + 1], bytes[index +
						2], 255);
					cursor.next().set(color);
					index += 3;
				}
			}
			catch (FormatException | IOException e) {
				throw new RuntimeException(e);
			}
		}

		private CalibratedAxis[] getCalibratedAxes(int fullres, int series) {
			return new CalibratedAxis[] { initAxis(Axes.X, fullres, series,
				metadata::getPixelsPhysicalSizeX, metadata::getPixelsSizeX), initAxis(
					Axes.Y, fullres, series, metadata::getPixelsPhysicalSizeY,
					metadata::getPixelsSizeY) };
		}

		private static DefaultLinearAxis initAxis(AxisType axisType, int fullres,
			int series, IntFunction<Length> pixelSize,
			IntFunction<PositiveInteger> imageSize)
		{
			// NB: metadata.getPixelsPhysicalSizeX/Y return the same pixel size for
			// each image in the resolution pyramid
			// The following 4 lines of code, calculate the correct pixel size for the
			// scaled down images.
			// This is a workaround until metadata.getPixelPysicalSizeX/Y are fixed.
			double fullResolutionPixelSize = pixelSize.apply(fullres).value(
				UNITS.MICROMETER).doubleValue();
			int fullResolutionImageSize = imageSize.apply(fullres).getValue();
			int lowResolutionImageSize = imageSize.apply(series).getValue();
			double lowResolutionPixelSize = fullResolutionPixelSize * getScale(
				fullResolutionImageSize, lowResolutionImageSize);
			return new DefaultLinearAxis(axisType, "microm", lowResolutionPixelSize);
		}

		private static double getScale(int fullSize, int downscaledSize) {
			long integerScale = fullSize / downscaledSize;
			if (fullSize / integerScale == downscaledSize) return integerScale;
			return (double) fullSize / (double) downscaledSize;
		}
	}
}
