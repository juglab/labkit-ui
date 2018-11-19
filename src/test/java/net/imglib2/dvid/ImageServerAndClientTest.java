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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ImageServerAndClientTest {

	private ImageServer server;

	@Before
	public void startServer() {
		server = new ImageServer();
	}

	@After
	public void stopServer() {
		server.close();
	}

	@Test
	public void testTypes() {
		testType(new UnsignedByteType(42));
		testType(new ByteType((byte) 42));
		testType(new UnsignedShortType(42));
		testType(new ShortType((short) 42));
		testType(new UnsignedIntType(42));
		testType(new IntType(42));
		testType(new UnsignedLongType(42));
		testType(new LongType(42));
		testType(new FloatType(42));
		testType(new DoubleType(42));
	}

	private <T extends NativeType<T>> void testType(T type) {
		Img<T> image = new ArrayImgFactory<>(type).create(1, 1, 1);
		image.forEach(x -> x.set(type));
		testImage(image);
	}

	private <T extends NativeType<T>> void testImage(Img<T> image) {
		ImageId id = ImageRepository.getInstance().addImage("image", image);
		@SuppressWarnings("unchecked")
		Img<T> result = (Img<T>) ImageClient.asCachedImg(id.getUrl());
		assertTrue(Intervals.equals(image, result));
		LoopBuilder.setImages(image, result).forEachPixel( Assert::assertEquals );
	}
}
