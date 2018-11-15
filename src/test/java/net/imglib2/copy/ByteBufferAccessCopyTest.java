package net.imglib2.copy;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.CharArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.LongArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ByteBufferAccessCopyTest {

	@Test
	public void testFromByteBuffer() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
		ByteArray access = new ByteArray(4);
		ByteBufferAccessCopy.fromByteBuffer(buffer, access);
		assertArrayEquals(new byte[]{1, 2, 3, 4}, access.getCurrentStorageArray());
	}

	@Test
	public void testToByteBuffer() {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		ByteArray access = new ByteArray(new byte[]{1, 2, 3, 4});
		ByteBufferAccessCopy.toByteBuffer(buffer, access);
		assertArrayEquals(new byte[]{1, 2, 3, 4}, buffer.array());
	}

	@Test
	public void testToSmallBufferToAccess() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
		ByteArray access = new ByteArray(5);
		ByteBufferAccessCopy.fromByteBuffer(buffer, access);
		assertArrayEquals(new byte[]{1, 2, 3, 4, 0}, access.getCurrentStorageArray());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testToLargeBufferToAccess() {
		ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
		ByteArray access = new ByteArray(4);
		ByteBufferAccessCopy.fromByteBuffer(buffer, access);
	}

	@Test
	public void testTypes() {
		testType(new ByteArray(8));
		testType(new CharArray(4));
		testType(new ShortArray(4));
		testType(new IntArray(2));
		testType(new LongArray(1));
		testType(new FloatArray(2));
		testType(new DoubleArray(1));
	}

	public void testType(ArrayDataAccess<?> access) {
		final ByteBuffer input = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
		final ByteBuffer output = ByteBuffer.allocate(8);
		ByteBufferAccessCopy.fromByteBuffer(input, access);
		ByteBufferAccessCopy.toByteBuffer(output, access);
		assertEquals(8, input.position());
		assertEquals(8, output.position());
		assertArrayEquals(input.array(), output.array());
	}
}
