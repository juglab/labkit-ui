package net.imglib2.labkit_rest_api.dvid;

import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.copy.ByteBufferImgCopy;
import net.imglib2.labkit_rest_api.dvid.metadata.PixelType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.nio.ByteBuffer;

public class ImageWrapper<T> implements ImageRepresentation {

	public static <T> ImageRepresentation create(RandomAccessibleInterval<T> image) {
		Type<?> type = (Type<?>) Util.getTypeFromInterval(image);
		return new ImageWrapper<>(PixelType.valueOf(type), image);
	}

	private final RandomAccessibleInterval<T> image;
	private final PixelType type;
	private final int bytesPerPixel;

	private ImageWrapper(PixelType type, RandomAccessibleInterval<T> image) {
		this.type = type;
		this.image = image;
		this.bytesPerPixel = ((RealType<?>) type.getType()).getBitsPerPixel() / 8;
	}

	@Override
	public PixelType typeSpecification() {
		return type;
	}

	@Override
	public Interval interval() {
		return image;
	}

	@Override
	public byte[] getBinaryData(Interval interval) {
		ByteBuffer buffer = ByteBuffer.allocate( (int) (bytesPerPixel * Intervals.numElements( interval )) );
		ByteBufferImgCopy.toByteBuffer(buffer, Views.interval(image, interval));
		return buffer.array();
	}
}
