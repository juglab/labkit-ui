package net.imglib2.labkit_rest_api.dvid;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.DoubleAccess;
import net.imglib2.img.basictypeaccess.FloatAccess;
import net.imglib2.img.basictypeaccess.IntAccess;
import net.imglib2.img.basictypeaccess.LongAccess;
import net.imglib2.img.basictypeaccess.ShortAccess;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BytesWrapper {

	private static class TypeStrategy<T> {

		private final Class<T> typeClass;
		private final BiFunction<byte[], long[], Img<T>> wrap;

		private <A> TypeStrategy(Class<T> typeClass, Function<ByteBuffer, A> accessFactory, BiFunction<A, long[], Img<T>> factory) {
			this.typeClass = typeClass;
			this.wrap = (bytes, dims) -> factory.apply(accessFactory.apply(ByteBuffer.wrap(bytes)), dims);
		}

		private <A> TypeStrategy(Class<T> typeClass, BiFunction<byte[], long[], Img<T>> factory) {
			this.typeClass = typeClass;
			this.wrap = factory;
		}
	}

	private static final List<TypeStrategy> strategies = Arrays.asList(
			new TypeStrategy<>(UnsignedByteType.class, ArrayImgs::unsignedBytes),
			new TypeStrategy<>(ByteType.class, ArrayImgs::bytes),
			new TypeStrategy<>(UnsignedShortType.class, ShortAccessToByteBuffer::new, ArrayImgs::unsignedShorts),
			new TypeStrategy<>(ShortType.class, ShortAccessToByteBuffer::new, ArrayImgs::shorts),
			new TypeStrategy<>(UnsignedIntType.class, IntAccessToByteBuffer::new, ArrayImgs::unsignedInts),
			new TypeStrategy<>(IntType.class, IntAccessToByteBuffer::new, ArrayImgs::ints),
			new TypeStrategy<>(UnsignedLongType.class, LongAccessToByteBuffer::new, ArrayImgs::unsignedLongs),
			new TypeStrategy<>(LongType.class, LongAccessToByteBuffer::new, ArrayImgs::longs),
			new TypeStrategy<>(DoubleType.class, DoubleAccessToByteBuffer::new, ArrayImgs::doubles),
			new TypeStrategy<>(FloatType.class, FloatAccessToByteBuffer::new, ArrayImgs::floats)
	);

	private static <T> Img<T> create(byte[] bytes, long[] dims, T type) {
		for(TypeStrategy<?> strategy : strategies)
			if(strategy.typeClass.isInstance(type))
				return (Img<T>) strategy.wrap.apply(bytes, dims);
		throw new UnsupportedOperationException("Type not supported");
	}

	private static class ByteAccessToByteBuffer implements ByteAccess {

		private final ByteBuffer buffer;

		private ByteAccessToByteBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public byte getValue(int index) {
			return buffer.get(index);
		}

		@Override
		public void setValue(int index, byte value) {
			buffer.put(index, value);
		}
	}

	private static class ShortAccessToByteBuffer implements ShortAccess {

		private final ByteBuffer buffer;

		private ShortAccessToByteBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}


		@Override
		public short getValue(int index) {
			return buffer.getShort(index);
		}

		@Override
		public void setValue(int index, short value) {
			buffer.putShort(index, value);
		}
	}

	private static class IntAccessToByteBuffer implements IntAccess {

		private final ByteBuffer buffer;

		private IntAccessToByteBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}


		@Override
		public int getValue(int index) {
			return buffer.getInt(index);
		}

		@Override
		public void setValue(int index, int value) {
			buffer.putInt(index, value);
		}
	}

	private static class LongAccessToByteBuffer implements LongAccess {

		private final ByteBuffer buffer;

		private LongAccessToByteBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}


		@Override
		public long getValue(int index) {
			return buffer.getLong(index);
		}

		@Override
		public void setValue(int index, long value) {
			buffer.putLong(index, value);
		}
	}

	private static class FloatAccessToByteBuffer implements FloatAccess {

		private final ByteBuffer buffer;

		private FloatAccessToByteBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}


		@Override
		public float getValue(int index) {
			return buffer.getFloat(index);
		}

		@Override
		public void setValue(int index, float value) {
			buffer.putFloat(index, value);
		}
	}

	private static class DoubleAccessToByteBuffer implements DoubleAccess {

		private final ByteBuffer buffer;

		private DoubleAccessToByteBuffer(ByteBuffer buffer) {
			this.buffer = buffer;
		}


		@Override
		public double getValue(int index) {
			return buffer.getDouble(index);
		}

		@Override
		public void setValue(int index, double value) {
			buffer.putDouble(index, value);
		}
	}
}
