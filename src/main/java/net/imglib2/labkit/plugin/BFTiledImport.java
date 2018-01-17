package net.imglib2.labkit.plugin;

import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.in.JPEGReader;
import loci.formats.in.ZeissCZIReader;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.MainFrame;
import net.imglib2.labkit.ParallelUtils;
import net.imglib2.labkit.inputimage.DefaultInputImage;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.Context;

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
		Img<ARGBType> out = openImage("/home/arzt/Documents/Datasets/Lung IMages/2017_08_03__0004-1.czi");
		new MainFrame(new Context(), new DefaultInputImage(out));
	}

	static Img<ARGBType> openImage(String filename) {
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
		return out;
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
