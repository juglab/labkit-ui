package net.imglib2.labkit.plugin;

import bdv.util.Bdv;
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
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.labkit.plugin.ui.SectionsDialog;
import net.imglib2.labkit.utils.ParallelUtils;
import net.imglib2.trainable_segmention.RevampUtils;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.xml.model.primitives.PositiveInteger;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class BFTiledImport {

	public static class Section {
		public Integer index;
		public ImgPlus<ARGBType> image;
	}

	public static void main(String... args) throws IOException, FormatException {
		Section section = openImage("/home/arzt/Documents/Datasets/Lung IMages/2017_08_03__0004-1.czi");
		BdvFunctions.show(section.image, "Image", Bdv.options().is2D());
	}

	public static Section openImage(String filename) {
		MyReader reader = new MyReader( filename );
		SectionsDialog dialog = SectionsDialog.show(initReader(filename));
		Section res = new Section();
		res.index = dialog.getSelectedSection();
		List<Integer> sectionIds = dialog.getSelectedSectionIndices();
		final int series = selectSectionResolution(filename, sectionIds);
		long[] dimensions = reader.getImgDimensions( series );
		int[] cellDimensions = reader.getCellDimensions( series );
		Img<ARGBType> out = new CellImgFactory<ARGBType>(cellDimensions).create( dimensions, new ARGBType() );
		List<Callable<Void>> chunks = ParallelUtils.chunkOperation(out, cellDimensions, cell -> reader.readToInterval( series, cell ) );
		ParallelUtils.executeInParallel(Executors.newFixedThreadPool(8), ParallelUtils.addShowProgress(chunks));
		res.image = imgPlus( filename, out, reader.printCalibration( series ) );
		return res;
	}

	private static ImgPlus< ARGBType > imgPlus( String filename, Img< ARGBType > out, CalibratedAxis[] axis )
	{
		ImgPlus<ARGBType> imgPlus = new ImgPlus<>(out, filename, axis);
		imgPlus.setSource(filename);
		return imgPlus;
	}

	private static int selectSectionResolution(String filename, List<Integer> sectionIds) {
		ImageReader reader = initReader(filename);
		List<String> list = sectionIds.stream().map(series -> {
			reader.setSeries(series);
			return reader.getSizeX() + " x " + reader.getSizeY();
		}).collect(Collectors.toList());
		Object result = JOptionPane.showInputDialog(null, "Select Image Resolution", "Labkit - Import Image", JOptionPane.PLAIN_MESSAGE, null, list.toArray(), list.get(0));
		if(result == null)
			throw new CancellationException();
		for(int i = 0; i < list.size(); i++) {
			if(list.get(i).equals(result)) {
				return sectionIds.get(i);
			}
		}
		return -1;
	}

	// -- Helper methods --

	private static ImageReader initReader(String filename) {
		ImageReader reader = initReader();
		readerSetFile( reader, filename );
		return reader;
	}

	private static ValuePair<ImageReader, IMetadata> initReaderAndMetaData(String filename) {
		ImageReader reader = initReader();
		IMetadata metadata = MetadataTools.createOMEXMLMetadata();
		reader.setMetadataStore( metadata );
		readerSetFile( reader, filename );
		return new ValuePair<>( reader, metadata );
	}

	private static void readerSetFile( ImageReader reader, String filename )
	{
		try {
			reader.setId(filename);
		} catch (FormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ImageReader initReader()
	{
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
			ValuePair< ImageReader, IMetadata > readerAndMetaData = initReaderAndMetaData( filename );
			reader = readerAndMetaData.getA();
			metadata = readerAndMetaData.getB();
		}

		private ImageReader getReader( int series )
		{
			ImageReader reader = this.reader;
			reader.setSeries( series );
			return reader;
		}

		private long[] getImgDimensions( int series ) {
			ImageReader reader = getReader( series );
			return new long[] { reader.getSizeX(), reader.getSizeY() };
		}

		private int[] getCellDimensions( int series) {
			ImageReader reader = getReader( series );
			return new int[] { reader.getOptimalTileWidth(), reader.getOptimalTileHeight() };
		}

		private void readToInterval(int series, RandomAccessibleInterval<ARGBType> interval) {
			int[] min = Intervals.minAsIntArray(interval);
			int[] size = Intervals.dimensionsAsIntArray(interval);
			ImageReader reader = getReader( series );
			byte[] bytes = RevampUtils.wrapException( () -> reader.openBytes(0, min[0], min[1], size[0], size[1]) );
			Cursor<ARGBType> cursor = Views.flatIterable(interval).cursor();
			int index = 0;
			while(cursor.hasNext()) {
				int color = ARGBType.rgba(bytes[index], bytes[index + 1], bytes[index + 2], 255);
				cursor.next().set(color);
				index += 3;
			}
		}

		private CalibratedAxis[] printCalibration(int series) {
			return new CalibratedAxis[] {
					initAxis(Axes.X, series, metadata::getPixelsPhysicalSizeX, metadata::getPixelsSizeX),
					initAxis(Axes.Y, series, metadata::getPixelsPhysicalSizeY, metadata::getPixelsSizeY)
			};
		}

		private static DefaultLinearAxis initAxis(AxisType axisType, int series, IntFunction<Length> pixelSize, IntFunction<PositiveInteger> imageSize) {
			// NB: metadata.getPixelsPhysicalSizeX/Y return the same pixel size for each image in the resolution pyramid
			// The following 4 lines of code, calculate the correct pixel size for the scaled down images.
			// This is a workaround until metadata.getPixelPysicalSizeX/Y are fixed.
			double fullResolutionPixelSize = pixelSize.apply(0).value(UNITS.MICROMETER).doubleValue();
			double fullResolutionImageSize = imageSize.apply(0).getValue();
			double lowResolutionImageSize = imageSize.apply(series).getValue();
			double lowResolutionPixelSize = fullResolutionPixelSize * fullResolutionImageSize / lowResolutionImageSize;
			System.out.println("Axis " + axisType + " = " + lowResolutionPixelSize + "microm");
			return new DefaultLinearAxis(axisType, "microm", lowResolutionPixelSize);
		}
	}
}
