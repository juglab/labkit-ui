
package labkit_cluster;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedByteType;
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
			N5Utils.saveBlock(source, writer, "segmentation", new long[] { 0, 0, 0 });
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createDataset(String path) {
		try {
			N5Writer writer = new N5FSWriter(path);
			writer.createDataset("segmentation", new long[] { 1000, 1000, 1000 },
				new int[] { 100, 100, 100 }, DataType.UINT8, new GzipCompression());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
