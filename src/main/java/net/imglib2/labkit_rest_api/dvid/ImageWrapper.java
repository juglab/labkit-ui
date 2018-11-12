package net.imglib2.labkit_rest_api.dvid;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
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
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImageWrapper<T> implements ImageRepresentation {

	private static final List<TypeStrategy<?>> strategies = Arrays.asList(
			new TypeStrategy<>(UnsignedByteType.class, "uint8", buffer -> (pixel -> buffer.put(pixel.getByte())), 1),
			new TypeStrategy<>(ByteType.class, "int8", buffer -> (pixel -> buffer.put(pixel.getByte())), 1),
			new TypeStrategy<>(UnsignedShortType.class, "uint16", buffer -> (pixel -> buffer.putShort(pixel.getShort())), 2),
			new TypeStrategy<>(ShortType.class, "int16", buffer -> (pixel -> buffer.putShort(pixel.getShort())), 2),
			new TypeStrategy<>(UnsignedIntType.class, "uint32", buffer -> (pixel -> buffer.putInt(pixel.getInt())), 4),
			new TypeStrategy<>(IntType.class, "int32", buffer -> (pixel -> buffer.putInt(pixel.getInt())), 4),
			new TypeStrategy<>(UnsignedLongType.class, "uint64", buffer -> (pixel -> buffer.putLong(pixel.getLong())), 8),
			new TypeStrategy<>(LongType.class, "int64", buffer -> (pixel -> buffer.putLong(pixel.getLong())), 8),
			new TypeStrategy<>(FloatType.class, "float32", buffer -> (pixel -> buffer.putFloat(pixel.getRealFloat())), 4),
			new TypeStrategy<>(DoubleType.class, "float64", buffer -> (pixel -> buffer.putDouble(pixel.getRealDouble())), 8)
	);

	private static class TypeStrategy<T> {

		private final Class<T> typeClass;
		private final String typeString;
		private final Function<ByteBuffer, Consumer<T>> pixelAdder;
		private final int bytesPerPixel;

		private TypeStrategy(Class<T> typeClass, String typeString, Function<ByteBuffer, Consumer<T>> pixelAdder, int bytesPerPixel) {
			this.typeClass = typeClass;
			this.typeString = typeString;
			this.pixelAdder = pixelAdder;
			this.bytesPerPixel = bytesPerPixel;
		}
	}

	public static <T> ImageRepresentation create(RandomAccessibleInterval<T> image) {
		Object type = Util.getTypeFromInterval(image);
		for(TypeStrategy<?> strategy : strategies)
			if(strategy.typeClass.isInstance(type))
				return new ImageWrapper<>((TypeStrategy<T>) strategy, image);
		throw new UnsupportedOperationException("Type not supported");
	}

	private final RandomAccessibleInterval<T> image;

	private final TypeStrategy<T> type;

	private ImageWrapper(TypeStrategy<T> type, RandomAccessibleInterval<T> image) {
		this.type = type;
		this.image = image;
	}

	@Override
	public String typeSpecification() {
		return type.typeString;
	}

	@Override
	public Interval interval() {
		return image;
	}

	@Override
	public byte[] getBinaryData(Interval interval) {
		ByteBuffer buffer = ByteBuffer.allocate( type.bytesPerPixel );
		final Consumer<T> addToBuffer = type.pixelAdder.apply(buffer);
		LoopBuilder.setImages(Views.interval(image, interval)).forEachPixel(addToBuffer);
		return buffer.array();
	}
}
