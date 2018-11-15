package net.imglib2.copy;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;

public class ByteBufferImgCopyTest {

	@Test
	public void testFromByteBuffer() {
		final byte[] bytes = {0, 1, 0, 2, 0, 3, 1, 0};
		final short[] expectedPixels = {1, 2, 3, 256};
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		final short[] pixels = new short[4];
		Img<UnsignedShortType> img = ArrayImgs.unsignedShorts(pixels, 2, 2);
		ByteBufferImgCopy.fromByteBuffer(buffer, img);
		assertArrayEquals(expectedPixels, pixels);
	}

	@Test
	public void testToByteBuffer() {
		final byte[] expectedBytes = {0, 1, 0, 2, 0, 3, 1, 0};
		final short[] pixels = {1, 2, 3, 256};
		ByteBuffer buffer = ByteBuffer.allocate(8);
		Img<UnsignedShortType> img = ArrayImgs.unsignedShorts(pixels, 2, 2);
		ByteBufferImgCopy.toByteBuffer(buffer, img);
		assertArrayEquals(expectedBytes, buffer.array());
	}
}
