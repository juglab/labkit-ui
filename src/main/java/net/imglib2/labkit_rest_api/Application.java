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
        Server server = new Server();
        shutdownServerAtExit(server);
        Main.main(args);
    }

    public static void shutdownServerAtExit(Server server) {
        // TODO: find less hacky way to shut down the server at the end
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }
}
