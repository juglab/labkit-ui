package net.imglib2.labkit_rest_api;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit_rest_api.dvid.ImageId;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import java.io.IOException;

/** Launches REST server and exposes tiny image. */
public class DummyApplication {

	public static void main(String... args) throws IOException {
		try (Server server = new Server()){
			addImage();
			System.out.println();
			System.in.read();
		}
	}

	private static void addImage() {
		ImageRepository imageRepository = ImageRepository.getInstance();
		byte[] array = {1, 2, 3, 4, 5, 6, 7, 8};
		long[] dims = {2, 2, 2};
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(array, dims);
		ImageId id = imageRepository.addImage("image", image);
		System.out.println("Expose image as: " + id.getUrl() + "/metadata");
	}
}
