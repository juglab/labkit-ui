
package labkit_cluter;

import bdv.util.BdvFunctions;
import labkit_cluster.MyN5;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.cell.CellGrid;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.IntStream;

public class MyN5Test {

	@Test
	public void testHdf5() {
		final String path = "/home/arzt/tmp/output/result";
		long[] dims = { 1000, 1000, 1000 };
		int[] cellDims = { 100, 100, 100 };
		RandomAccessibleInterval<UnsignedByteType> source = ArrayImgs.unsignedBytes(
			IntStream.of(cellDims).mapToLong(x -> x).toArray());
		Views.iterable(source).forEach(pixel -> pixel.set(127));
		MyN5.createDataset(path, new CellGrid(dims, cellDims));
		MyN5.writeBlock(path, source);

	}

	public static void main(String... args) throws IOException {
		N5FSReader reader = new N5FSReader("/home/arzt/tmp/output/result.xml");
		RandomAccessibleInterval<UnsignedByteType> result = N5Utils
			.openWithDiskCache(reader, "segmentation", new UnsignedByteType());
		BdvFunctions.show(result, "N5!!");
	}
}
