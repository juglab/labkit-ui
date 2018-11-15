package net.imglib2.copy;

import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;

import java.nio.ByteBuffer;

public class ByteBufferAccessCopy {

	public static int bytesPerElement(ArrayDataAccess<?> access) {
		return ByteBufferArrayCopy.bytesPerElement(access.getCurrentStorageArray());
	}

	public static void toByteBuffer(ByteBuffer buffer, ArrayDataAccess<?> access) {
		int bytesPerElement = bytesPerElement(access);
		toByteBuffer(buffer, access, buffer.remaining() / bytesPerElement);
	}

	public static void fromByteBuffer(ByteBuffer buffer, ArrayDataAccess<?> access) {
		int bytesPerElement = bytesPerElement(access);
		fromByteBuffer(buffer, access, buffer.remaining() / bytesPerElement);
	}

	public static void toByteBuffer(ByteBuffer buffer, ArrayDataAccess<?> access, int numElements) {
		ByteBufferArrayCopy.toByteBuffer(buffer, access.getCurrentStorageArray(), 0, numElements);
	}

	public static void fromByteBuffer(ByteBuffer buffer, ArrayDataAccess<?> access, int numElements) {
		ByteBufferArrayCopy.fromByteBuffer(buffer, access.getCurrentStorageArray(), 0, numElements);
	}
}

