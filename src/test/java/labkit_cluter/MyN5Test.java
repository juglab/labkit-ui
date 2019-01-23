
package labkit_cluter;

import bdv.util.BdvFunctions;
import labkit_cluster.MyN5;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;
import org.janelia.saalfeldlab.n5.N5FSReader;
import org.janelia.saalfeldlab.n5.imglib2.N5Utils;
import org.junit.Test;

import java.io.IOException;

public class MyN5Test {

	@Test
	public void testHdf5() {
		RandomAccessibleInterval<UnsignedByteType> source = ArrayImgs.unsignedBytes(
			100, 100, 100);
		Views.iterable(source).forEach(pixel -> pixel.set(127));
		final String path = "/home/arzt/tmp/output/result";
		MyN5.createDataset(path);
		MyN5.writeBlock(path, source);

	}

	public static void main(String... args) throws IOException {
		N5FSReader reader = new N5FSReader("/home/arzt/tmp/output/result.xml");
		RandomAccessibleInterval<UnsignedByteType> result = N5Utils
			.openWithDiskCache(reader, "segmentation", new UnsignedByteType());
		BdvFunctions.show(result, "N5!!");
	}
}
