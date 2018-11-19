package net.imglib2.dvid;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
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
import net.imglib2.util.Intervals;
import org.junit.Test;

public class ImageServerAndClientTest {

	@Test
	public void test() {
		try(ImageServer server = new ImageServer()) {
			test(new UnsignedByteType(42));
			test(new ByteType((byte) 42));
			test(new UnsignedShortType(42));
			test(new ShortType((short) 42));
			test(new UnsignedIntType(42));
			test(new IntType(42));
			test(new UnsignedLongType(42));
			test(new LongType(42));
			test(new FloatType(42));
			test(new DoubleType(42));
			System.out.println("sucess");
		}
	}

	public static <T extends NativeType<T>> void test(T type) {
		Img<T> image = new ArrayImgFactory<>(type).create(1, 1, 1);
		image.forEach(x -> x.set(type));
		testImage(image);
	}

	public static <T extends NativeType<T>> void testImage(Img<T> image) {
		ImageId id = ImageRepository.getInstance().addImage("image", image);
		Img<T> result = (Img<T>) ImageClient.asCachedImg(id.getUrl());
		if( !Intervals.equals(image, result) )
			throw new AssertionError("intervals different");
		LoopBuilder.setImages(image, result).forEachPixel(
				(a,b) -> {
					if( !a.equals(b) )
						throw new AssertionError("wrong pixel value");
				}
		);
	}
}
