
package bdv.export;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class HDF5SaverTest {

	@Test
	public void testSave4d() throws IOException {
		File xml = File.createTempFile("test", ".xml");
		RandomAccessibleInterval<UnsignedShortType> image = ArrayImgs
			.unsignedShorts(2, 3, 4, 5);
		new HDF5Saver().save(xml.getAbsolutePath(), image);
	}

	@Test
	public void testSave2d() throws IOException {
		File xml = File.createTempFile("test", ".xml");
		RandomAccessibleInterval<UnsignedShortType> image = ArrayImgs
			.unsignedShorts(2, 3);
		new HDF5Saver().save(xml.getAbsolutePath(), image);
	}
}
