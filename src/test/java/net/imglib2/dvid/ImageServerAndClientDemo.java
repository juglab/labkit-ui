package net.imglib2.dvid;

import bdv.util.BdvFunctions;
import bdv.util.BdvSource;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import java.io.IOException;

/** Launches REST server and exposes tiny image. */
public class ImageServerAndClientDemo {

	public static void main(String... args) throws IOException {
		try (ImageServer server = new ImageServer()){
			String url = addImageToServer(testImage());
			showImage(url);
			waitForKeyPress();
		}
	}

	private static Img<UnsignedByteType> testImage() {
		byte[] array = {1, 2, 3, 4, 5, 6, 7, 8};
		long[] dims = {2, 2, 2};
		return ArrayImgs.unsignedBytes(array, dims);
	}

	private static String addImageToServer(Img<UnsignedByteType> image) {
		ImageRepository imageRepository = ImageRepository.getInstance();
		ImageId id = imageRepository.addImage("image", image);
		System.out.println("Expose image as: " + id.getUrl() + "/metadata");
		return id.getUrl();
	}

	public static void showImage(String url) {
		BdvSource source = BdvFunctions.show(ImageClient.asCachedImg(url), "image");
		source.setDisplayRange(0, 8);
	}

	public static void waitForKeyPress() throws IOException {
		System.out.println("Press any key to quit the server.");
		System.in.read();
	}

}
