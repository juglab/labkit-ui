
package labkit_cluster;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.DataType;
import org.janelia.saalfeldlab.n5.GzipCompression;
import org.janelia.saalfeldlab.n5.N5FSWriter;
import org.janelia.saalfeldlab.n5.N5Writer;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;

import java.io.IOException;

public class MyN5 {

	public static void writeBlock(String path,
		RandomAccessibleInterval<UnsignedByteType> source)
	{
		try {
			N5Writer writer = new N5FSWriter(path);
			N5Utils.saveBlock(Views.zeroMin(source), writer, "segmentation",
				gridOffset(source));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createDataset(String path, CellGrid grid) {
		try {
			N5Writer writer = new N5FSWriter(path);
			writer.createDataset("segmentation", grid.getImgDimensions(),
				getCellDimensions(grid), DataType.UINT8, new GzipCompression());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static int[] getCellDimensions(CellGrid grid) {
		int[] cellDimensions = new int[grid.numDimensions()];
		grid.cellDimensions(cellDimensions);
		return cellDimensions;
	}

	private static long[] gridOffset(Interval interval) {
		long[] offset = Intervals.minAsLongArray(interval);
		long[] dimensions = Intervals.dimensionsAsLongArray(interval);
		for (int i = 0; i < offset.length; i++)
			offset[i] /= dimensions[i];
		return offset;
	}
}
