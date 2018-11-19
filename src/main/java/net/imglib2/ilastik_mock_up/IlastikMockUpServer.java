package net.imglib2.ilastik_mock_up;

import net.imglib2.dvid.ImageController;
import net.imglib2.dvid.ImageRepository;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Server for the Labkit REST API.
 */
public final class IlastikMockUpServer implements AutoCloseable {

	private static final ResourceConfig CONFIG = new ResourceConfig()
			.registerClasses(SegmentationService.class, ImageController.class);

	private final HttpServer server;

	public IlastikMockUpServer() {
		this("127.0.0.1", 8571);
	}

	public IlastikMockUpServer(String host, int port) {
		URI uri = URI.create(String.format("http://%s:%d", host, port));
		server = GrizzlyHttpServerFactory.createHttpServer(uri, CONFIG);
		try {
			ImageRepository.getInstance().setUrl(uri.toString());
			server.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		server.shutdownNow();
	}
}
