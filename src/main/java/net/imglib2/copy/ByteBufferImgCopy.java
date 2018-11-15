package net.imglib2.copy;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.SingleCellArrayImg;
import net.imglib2.img.NativeImg;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.numeric.integer.GenericByteType;
import net.imglib2.type.numeric.integer.GenericIntType;
import net.imglib2.type.numeric.integer.GenericLongType;
import net.imglib2.type.numeric.integer.GenericShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ByteBufferImgCopy {

	public static <T> void toByteBuffer(ByteBuffer buffer, RandomAccessibleInterval<T> image) {
		if (image instanceof SingleCellArrayImg || image instanceof ArrayImg)
			arrayImgToByteBuffer((NativeImg<?, ?>) image, buffer);
		else
			anyRandomAccessibleIntervalToByteBuffer(image, buffer);
	}

	public static <T> void fromByteBuffer(ByteBuffer buffer, RandomAccessibleInterval<T> image) {
		if (image instanceof SingleCellArrayImg || image instanceof ArrayImg)
			arrayImgFromByteBuffer((NativeImg<?, ?>) image, buffer);
		else anyRandomAccessibleIntervalFromByteBuffer(buffer, image);
	}

	private static final List<Strategy<?>> strategies = Arrays.asList(
			new Strategy<>(
					GenericByteType.class,
					Byte.BYTES,
					buffer -> (pixel -> buffer.put(pixel.getByte())),
					buffer -> (pixel -> pixel.setByte(buffer.get()))
			),
			new Strategy<>(
					GenericShortType.class,
					Short.BYTES,
					buffer -> (pixel -> buffer.putShort(pixel.getShort())),
					buffer -> (pixel -> pixel.setShort(buffer.getShort()))
			),
			new Strategy<>(
					GenericIntType.class,
					Integer.BYTES,
					buffer -> (pixel -> buffer.putInt(pixel.getInt())),
					buffer -> (pixel -> pixel.setInt(buffer.getInt()))
			),
			new Strategy<>(
					GenericLongType.class,
					Long.BYTES,
					buffer -> (pixel -> buffer.putLong(pixel.getLong())),
					buffer -> (pixel -> pixel.setLong(buffer.getLong()))
			),
			new Strategy<>(
					FloatType.class,
					Float.BYTES,
					buffer -> (pixel -> buffer.putFloat(pixel.getRealFloat())),
					buffer -> (pixel -> pixel.set(buffer.getFloat()))
			),
			new Strategy<>(
					DoubleType.class,
					Double.BYTES,
					buffer -> (pixel -> buffer.putDouble(pixel.getRealDouble())),
					buffer -> (pixel -> pixel.set(buffer.getDouble()))
			)
	);

	private static class Strategy<T> {

		private final Class<T> typeClass;
		private final int bytesPerPixel;
		private final Function<ByteBuffer, Consumer<T>> pixelToByteBuffer;
		private final Function<ByteBuffer, Consumer<T>> pixelFromByteBuffer;

		private Strategy(Class<T> type, int bytesPerPixel, Function<ByteBuffer, Consumer<T>> pixelToByteBuffer, Function<ByteBuffer, Consumer<T>> pixelFromByteBuffer) {
			this.typeClass = type;
			this.bytesPerPixel = bytesPerPixel;
			this.pixelToByteBuffer = pixelToByteBuffer;
			this.pixelFromByteBuffer = pixelFromByteBuffer;
		}
	}

	private static <T> Strategy<T> getStrategy(T typeFromInterval) {
		for (Strategy<?> strategy : strategies)
			if (strategy.typeClass.isInstance(typeFromInterval))
				return (Strategy<T>) strategy;
		throw new UnsupportedOperationException();
	}

	private static void arrayImgToByteBuffer(NativeImg<?, ?> image, ByteBuffer buffer) {
		int numElements = (int) Intervals.numElements(image);
		ByteBufferAccessCopy.toByteBuffer(buffer, (ArrayDataAccess<?>) image.update(null), numElements);
	}

	private static <T> void anyRandomAccessibleIntervalToByteBuffer(RandomAccessibleInterval<T> image, ByteBuffer buffer) {
		Strategy<T> strategy = getStrategy(Util.getTypeFromInterval(image));
		LoopBuilder.setImages(image).forEachPixel(strategy.pixelToByteBuffer.apply(buffer));
	}

	private static void arrayImgFromByteBuffer(NativeImg<?, ?> image, ByteBuffer buffer) {
		int numElements = (int) Intervals.numElements(image);
		ByteBufferAccessCopy.fromByteBuffer( buffer, (ArrayDataAccess<?>) image.update(null), numElements);
	}

	private static <T> void anyRandomAccessibleIntervalFromByteBuffer(ByteBuffer buffer, RandomAccessibleInterval<T> image) {
		Strategy<T> strategy = getStrategy(Util.getTypeFromInterval(image));
		LoopBuilder.setImages(image).forEachPixel(strategy.pixelFromByteBuffer.apply(buffer));
	}
}
