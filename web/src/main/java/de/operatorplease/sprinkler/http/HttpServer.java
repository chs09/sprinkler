package de.operatorplease.sprinkler.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;

import de.operatorplease.sprinkler.http.HttpRequest.Method;

/**
 * Represents a simple HTTP server (a facade around {@link com.sun.net.httpserver.HttpServer for unit testing.
 * The server is started after invoking the {@link HttpServer#start()} method. It's a good practice
 * to shutdown it with {@link HttpServer#stop()} method.
 */
public class HttpServer implements Closeable {

    private com.sun.net.httpserver.HttpServer sunHttpServer;
    private List<HttpHandlerConfig> handlers = new ArrayList<>();

    /**
     * Adds a new handler to the server to a path.
     */
    public HttpServer addHandler(String path, HttpHandler handler) {
        return addHandler(path, handler, null);
    }

    /**
     * Adds a new handler to the server to a path with an authenticator.
     */
    public HttpServer addHandler(String path, HttpHandler handler, Authenticator authenticator) {
        handlers.add(new HttpHandlerConfig(path, handler, authenticator));
        return this;
    }

    /**
     * Starts up the current server on a free port on the localhost.
     */
    public HttpServer start() {
        return start(0);
    }

    /**
     * Starts up the server on the provided port on the provided port on the localhost.
     */
    public HttpServer start(int port) {
        return start(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));
    }

    /**
     * Starts up the server on provided address.
     */
    public HttpServer start(InetSocketAddress address) {
        try {
            sunHttpServer = com.sun.net.httpserver.HttpServer.create(address, 50);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (HttpHandlerConfig config : handlers) {
            HttpContext context = sunHttpServer.createContext(config.path, httpExchange -> {
                try {
                    Headers requestHeaders = httpExchange.getRequestHeaders();
                    HttpResponse response = new HttpResponse();
                    
                    String path = trimLeftSlash(makeRelativeURI(config.path, httpExchange.getRequestURI()));
                    
                    Method method = Method.valueOf(httpExchange.getRequestMethod());
                    try {
	                    config.httpHandler.handle(new HttpRequest(method, path,
	                            httpExchange.getRequestURI(), httpExchange.getProtocol(), requestHeaders,
	                            readFromStream(httpExchange.getRequestBody())), response);
                    } catch (HttpException e) {
                    	response = new HttpResponse();
                    	response.setStatusCode(e.getStatus());
                    	response.setBody(e.getMessage());
                    }
                    
                    for (Map.Entry<String, List<String>> e : response.getHeaders().entrySet()) {
                        httpExchange.getResponseHeaders().put(e.getKey(), e.getValue());
                    }

                    byte[] byteBody = response.getBody();
                    if(byteBody != null && byteBody.length > 0) {
                    	if(!httpExchange.getResponseHeaders().containsKey("Content-Type")) {
                    		String contentType = response.getContentType();
                    		if(contentType != null) {
                    			if(response.getCharset() != null) {
                    				contentType += "; charset=" + response.getCharset().name();
                    			}
                    			List<String> value = Collections.singletonList( contentType );
                    			httpExchange.getResponseHeaders().put("Content-Type", value );
                    		}
                    	}
                    	httpExchange.sendResponseHeaders(response.getStatusCode(), byteBody.length);
                        httpExchange.getResponseBody().write(byteBody);
                    } else {
                    	httpExchange.sendResponseHeaders(response.getStatusCode(), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    httpExchange.close();
                }
            });
            if (config.authenticator != null) {
                context.setAuthenticator(config.authenticator);
            }
        }
        sunHttpServer.start();
        return this;
    }
    
    private String trimLeftSlash(String path) {
    	return path.startsWith("/") ? path.substring(1) : path;
    }
    
    private String makeRelativeURI(String rootURI, URI requestURI) {
    	rootURI = trimLeftSlash(rootURI);
    	String uri = trimLeftSlash(requestURI.getPath());
    	if (rootURI.endsWith("/"))
    		return uri.substring(rootURI.length() - 1);
    	else
    		return uri.substring(rootURI.length());
	}

	/**
     * Stops the current server and frees resources.
     */
    public void stop() {
        sunHttpServer.stop(0);
    }

    /**
     * Invokes {@link HttpServer#stop()}.
     */
    @Override
    public void close() throws IOException {
        stop();
    }

    /**
     * Get the port on which server has been started.
     */
    public int getPort() {
        return sunHttpServer.getAddress().getPort();
    }

    /**
     * Gets the host on which server has been bound.
     */
    public String getBindHost() {
        return sunHttpServer.getAddress().getHostName();
    }

    /**
     * Reads the provided input stream to a string in the UTF-8 encoding
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static class HttpHandlerConfig {

        private final String path;
        private final HttpHandler httpHandler;
        private final Authenticator authenticator;

        HttpHandlerConfig(String path, HttpHandler httpHandler, Authenticator authenticator) {
            this.path = path;
            this.httpHandler = httpHandler;
            this.authenticator = authenticator;
        }
    }
}
