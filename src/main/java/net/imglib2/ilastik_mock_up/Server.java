package net.imglib2.ilastik_mock_up;

import net.imglib2.labkit_rest_api.ImageController;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Server for the Labkit REST API.
 */
public final class Server implements AutoCloseable {

	private static final ResourceConfig CONFIG = new ResourceConfig()
			.registerClasses(SegmentationService.class, ImageController.class);

	private final HttpServer server;

	public Server() {
		this("127.0.0.1", 8571);
	}

	public Server(String host, int port) {
		URI uri = URI.create(String.format("http://%s:%d/", host, port));
		server = GrizzlyHttpServerFactory.createHttpServer(uri, CONFIG);
		try {
			server.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		server.shutdownNow();
	}
}
