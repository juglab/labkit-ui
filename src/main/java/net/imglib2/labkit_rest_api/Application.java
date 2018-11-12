package net.imglib2.labkit_rest_api;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labkit.Main;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Launcher for the Labkit with the REST server.
 */
public class Application {
    public static void main(String... args) {
        setupRepositories();

        Server server = new Server();
        server.start();
        // Hacky way to stop the server at the VM shutdown.
		// TODO: Find a better way to stop the server.
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        Main.main(args);
    }

    private static void setupRepositories() {
        ImageRepository imageRepository = ImageRepository.getInstance();
        byte[] array = {1, 2, 3, 4, 5, 6, 7, 8};
        long[] dims = {2, 2, 2};
        Img<UnsignedByteType> image = ArrayImgs.unsignedBytes(array, dims);
        imageRepository.addImage("image", image);
    }
}
