package net.imglib2.labkit_rest_api;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Server for the Labkit REST API.
 */
public final class Server {
    private static final ResourceConfig CONFIG = new ResourceConfig().packages("net.imglib2.labkit_rest_api");

    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 8572;

    private final HttpServer server;

    public Server(String host, int port) {
        URI uri = URI.create(String.format("http://%s:%d/", host, port));
        server = GrizzlyHttpServerFactory.createHttpServer(uri, CONFIG);
    }

    public Server(String host) {
        this(host, DEFAULT_PORT);
    }

    public Server() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public void start() {
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        server.shutdownNow();
    }
}
