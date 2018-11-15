package net.imglib2.copy;

import java.nio.ByteBuffer;

public class ByteBufferArrayCopy {

	public static void fromByteBuffer(ByteBuffer buffer, Object array, int offset, int length) {
		if(array instanceof byte[])
			buffer.get((byte[]) array, offset, length);
		else if(array instanceof char[]) {
			buffer.asCharBuffer().get((char[]) array, offset, length);
			buffer.position(buffer.position() + length * Character.BYTES);
		} else if(array instanceof short[]) {
			buffer.asShortBuffer().get((short[]) array, offset, length);
			buffer.position(buffer.position() + length * Short.BYTES);
		} else if(array instanceof int[]) {
			buffer.asIntBuffer().get((int[]) array, offset, length);
			buffer.position(buffer.position() + length * Integer.BYTES);
		} else if(array instanceof long[]) {
			buffer.asLongBuffer().get((long[]) array, offset, length);
			buffer.position(buffer.position() + length * Long.BYTES);
		} else if(array instanceof float[]) {
			buffer.asFloatBuffer().get((float[]) array, offset, length);
			buffer.position(buffer.position() + length * Float.BYTES);
		} else if(array instanceof double[]) {
			buffer.asDoubleBuffer().get((double[]) array, offset, length);
			buffer.position(buffer.position() + length * Double.BYTES);
		} else
			throw new UnsupportedOperationException();
	}

	public static void toByteBuffer(ByteBuffer buffer, Object array, int offset, int length) {
		if(array instanceof byte[])
			buffer.put((byte[]) array, offset, length);
		else if(array instanceof char[]) {
			buffer.asCharBuffer().put((char[]) array, offset, length);
			buffer.position(buffer.position() + length * Character.BYTES);
		} else if(array instanceof short[]) {
			buffer.asShortBuffer().put((short[]) array, offset, length);
			buffer.position(buffer.position() + length * Short.BYTES);
		} else if(array instanceof int[]) {
			buffer.asIntBuffer().put((int[]) array, offset, length);
			buffer.position(buffer.position() + length * Integer.BYTES);
		} else if(array instanceof long[]) {
			buffer.asLongBuffer().put((long[]) array, offset, length);
			buffer.position(buffer.position() + length * Long.BYTES);
		} else if(array instanceof float[]) {
			buffer.asFloatBuffer().put((float[]) array, offset, length);
			buffer.position(buffer.position() + length * Float.BYTES);
		} else if(array instanceof double[]) {
			buffer.asDoubleBuffer().put((double[]) array, offset, length);
			buffer.position(buffer.position() + length * Double.BYTES);
		} else
			throw new UnsupportedOperationException();
	}

	public static int bytesPerElement(Object array) {
		if(array instanceof byte[]) return Byte.BYTES;
		if(array instanceof char[]) return Character.BYTES;
		if(array instanceof short[]) return Short.BYTES;
		if(array instanceof int[]) return Integer.BYTES;
		if(array instanceof long[]) return Long.BYTES;
		if(array instanceof float[]) return Float.BYTES;
		if(array instanceof double[]) return Double.BYTES;
		throw new UnsupportedOperationException();
	}
}
