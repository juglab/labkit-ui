package net.imglib2.labkit.plugin;

import bdv.util.BdvFunctions;
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
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import ome.units.UNITS;
import ome.units.quantity.Length;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BFTiledImport {

	public static void main(String... args) throws IOException, FormatException {
		ImgPlus<ARGBType> out = openImage("/home/arzt/Documents/Datasets/Lung IMages/2017_08_03__0004-1.czi");
		BdvFunctions.show(out, "Image");
	}

	static ImgPlus<ARGBType> openImage(String filename) {
		final int series = selectSeries(filename);
		ThreadLocal<ImageReader> tl = ThreadLocal.withInitial(() -> {
			ImageReader reader = initReader(filename);
			reader.setSeries(series);
			return reader;
		});
		ImageReader reader = tl.get();
		int sizeX = reader.getSizeX();
		int sizeY = reader.getSizeY();
		int[] cellDimensions = {1024, 1024};
		CellGrid grid = new CellGrid(new long[]{sizeX, sizeY}, cellDimensions);
		Img<ARGBType> out = new CellImgFactory<ARGBType>(cellDimensions).create(
				grid.getImgDimensions(), new ARGBType()
		);
		List<Callable<Void>> chunks = ParallelUtils.chunkOperation(out, cellDimensions, cell -> {
			try {
				readToInterval(tl.get(), cell);
			} catch (FormatException | IOException e) {
				throw new RuntimeException(e);
			}
		});
		ParallelUtils.executeInParallel(Executors.newFixedThreadPool(8), ParallelUtils.addShowProgress(chunks));
		CalibratedAxis[] axis = printCalibration(filename, series);
		ImgPlus<ARGBType> imgPlus = new ImgPlus<>(out, filename, axis);
		imgPlus.setSource(filename);
		return imgPlus;
	}

	private static CalibratedAxis[] printCalibration(String filename, int series) {
		ClassList<IFormatReader> cl = new ClassList<>(IFormatReader.class);
		cl.addClass(JPEGReader.class);
		cl.addClass(ZeissCZIReader.class);
		ImageReader reader = new ImageReader(cl);
		IMetadata metadata = MetadataTools.createOMEXMLMetadata();
		reader.setMetadataStore(metadata);
		try {
			reader.setId(filename);
		} catch (FormatException | IOException e) {
			throw new RuntimeException(e);
		}
		return new CalibratedAxis[] {
				initAxis(Axes.X, metadata.getPixelsPhysicalSizeX(series)),
				initAxis(Axes.Y, metadata.getPixelsPhysicalSizeY(series))
		};
	}

	private static DefaultLinearAxis initAxis(AxisType axisType, Length physicalSize) {
		double scale = physicalSize.value(UNITS.MICROMETER).doubleValue();
		System.out.println("Axis " + axisType + " = " + scale + "microm");
		return new DefaultLinearAxis(axisType, "microm", scale);
	}

	private static int selectSeries(String filename) {
		ImageReader reader = initReader(filename);
		List<String> list = IntStream.range(0, reader.getSeriesCount()).mapToObj(series -> {
			reader.setSeries(series);
			return reader.getSizeX() + " x " + reader.getSizeY();
		}).collect(Collectors.toList());
		Object result = JOptionPane.showInputDialog(null, "Select Image Resolution", "Labkit - Import Image", JOptionPane.PLAIN_MESSAGE, null, list.toArray(), list.get(0));
		if(result == null)
			throw new CancellationException();
		return list.indexOf(result);
	}

	private static ImageReader initReader(String filename) {
		try {
			ClassList<IFormatReader> cl = new ClassList<>(IFormatReader.class);
			cl.addClass(JPEGReader.class);
			cl.addClass(ZeissCZIReader.class);
			ImageReader reader = new ImageReader(cl);
			reader.setId(filename);
			return reader;
		} catch (FormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void readToInterval(ImageReader reader, RandomAccessibleInterval<ARGBType> interval) throws FormatException, IOException {
		int[] min = Intervals.minAsIntArray(interval);
		int[] size = Intervals.dimensionsAsIntArray(interval);
		byte[] bytes = reader.openBytes(0, min[0], min[1], size[0], size[1]);
		Cursor<ARGBType> cursor = Views.flatIterable(interval).cursor();
		int index = 0;
		while(cursor.hasNext()) {
			int color = ARGBType.rgba(bytes[index], bytes[index + 1], bytes[index + 2], 255);
			cursor.next().set(color);
			index += 3;
		}
	}
}
