package net.imglib2.labkit_rest_api;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.Main;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DummyImageServerApplication {

	public static void main(final String[] args) {
		Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(
				new byte[]{1,2,3,4,5,6,7,8},
				2, 2, 2
		);
		ImageRepository.getInstance().addImage("image", image);
		SpringApplication.run(DummyImageServerApplication.class, args);
	}
}
