package net.imglib2.labkit.imageserver;

import net.imglib2.labkit.Main;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(final String[] args) {
		Main.start("/home/arzt/Documents/Datasets/img_TL199_Chgreen.tif");
		SpringApplication.run(Application.class, args);
	}
}
