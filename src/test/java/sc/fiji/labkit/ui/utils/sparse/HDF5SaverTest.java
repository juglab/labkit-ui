
package sc.fiji.labkit.ui.utils.sparse;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import sc.fiji.labkit.ui.inputimage.SpimDataInputImage;
import net.imglib2.test.ImgLib2Assert;
import net.imglib2.test.RandomImgs;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.junit.Test;
import sc.fiji.labkit.ui.utils.HDF5Saver;

import java.io.File;
import java.io.IOException;

public class HDF5SaverTest {

	@Test
	public void testSave4d() throws IOException {
		File xml = File.createTempFile("test", ".xml");
		RandomAccessibleInterval<UnsignedShortType> image = ArrayImgs
			.unsignedShorts(2, 3, 4, 5);
		new HDF5Saver(image, xml.getAbsolutePath()).writeAll();
	}

	@Test
	public void testSave2d() throws IOException {
		File xml = File.createTempFile("test", ".xml");
		RandomAccessibleInterval<UnsignedShortType> image = ArrayImgs
			.unsignedShorts(2, 3);
		HDF5Saver saver = new HDF5Saver(image, xml.getAbsolutePath());
		saver.writeXmlAndHdf5();
		ImgLib2Assert.assertImageEquals(image, openImage(xml), Object::equals);
	}

	@Test
	public void testPartitionedWriting() throws IOException {
		File xml = File.createTempFile("test", ".xml");
		Img<UnsignedShortType> image = RandomImgs.seed(42).nextImage(
			new UnsignedShortType(), 2, 3, 4, 5);
		HDF5Saver saver = new HDF5Saver(image, xml.getAbsolutePath());
		saver.setPartitions(1, 1);
		saver.writeAll();
		ImgLib2Assert.assertImageEquals(image, openImage(xml), Object::equals);
	}

	@Test
	public void testPartitionedWriting2() throws IOException {
		File xml = File.createTempFile("test", ".xml");
		Img<UnsignedShortType> image = RandomImgs.seed(42).nextImage(
			new UnsignedShortType(), 2, 3, 4, 5);
		HDF5Saver saver = new HDF5Saver(image, xml.getAbsolutePath());
		saver.setPartitions(1, 1);
		saver.writeXmlAndHdf5();
		for (int i = 0; i < saver.numberOfPartitions(); i++)
			saver.writePartition(i);
		ImgLib2Assert.assertImageEquals(image, openImage(xml), Object::equals);
	}

	private RandomAccessibleInterval<? extends NumericType<?>> openImage(
		File xml)
	{
		return new SpimDataInputImage(xml.getAbsolutePath(), 0)
			.imageForSegmentation();
	}
}
