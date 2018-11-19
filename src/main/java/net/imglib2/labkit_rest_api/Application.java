package net.imglib2.labkit_rest_api;

import net.imglib2.ilastik_mock_up.IlastikMockUpServer;
import net.imglib2.labkit.Main;

/**
 * Launcher for the Labkit with the REST server.
 */
public class Application {

    public static void main(String... args) {
        IlastikMockUpServer server = new IlastikMockUpServer();
        shutdownServerAtExit(server);
        Main.main(args);
    }

    public static void shutdownServerAtExit(IlastikMockUpServer server) {
        // TODO: find less hacky way to shut down the server at the end
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }
}
