package net.imglib2.dvid;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public final class ImageServer implements AutoCloseable {

	private static final ResourceConfig CONFIG = new ResourceConfig()
			.registerClasses(ImageController.class);

	private final HttpServer server;

	public ImageServer() {
		this("127.0.0.1", 8572);
	}

	public ImageServer(String host, int port) {
		URI uri = URI.create(String.format("http://%s:%d", host, port));
		server = GrizzlyHttpServerFactory.createHttpServer(uri, CONFIG);
		try {
			ImageRepository.getInstance().setUrl(uri.toString());
			server.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		server.shutdownNow();
	}
}
