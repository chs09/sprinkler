package de.operatorplease.sprinkler.http;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.net.httpserver.Headers;

/**
 * Represents an HTTP request from an HTTP client
 */
public class HttpRequest {

	public enum Method {
		GET, HEAD, POST, PUT, OPTION, DELETE, CONNECT, OPTIONS, TRACE, PATCH
	}
	
    private final Method method;
    private final URI uri;
    private final String path;
    private final Map<String, String> queryParameters;
    private final String protocolVersion;
    private final Headers headers;
    private final String body;

    public HttpRequest(Method method, String path, URI uri, String protocolVersion, Headers headers, String body) {
        this.method = method;
        this.uri = uri;
        this.path = path;
        this.protocolVersion = protocolVersion;
        this.headers = headers;
        this.body = body;
        this.queryParameters = getQueryParameters(uri);
    }

    public Method getMethod() {
        return method;
    }

    public URI getUri() {
        return uri;
    }
    
    public String getPath() {
		return path;
	}

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public String getQueryParameter(String name) {
        return queryParameters.get(name);
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public Headers getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.getFirst(key);
    }

    public String getContentType() {
        return getHeader("Content-Type");
    }

    public String getBody() {
        return body;
    }

    /**
     * Gets the query parameters from the request body as a {@link Map}. The query parameters are
     * URI-encoded, and we should decode them when populating the map. In case we have several
     * parameters with the same name, the last one wins.
     */
    public Map<String, String> getQueryParametersFromBody() {
        return toMap(body);
    }

    /**
     * Gets the query parameters from the provided URI as a {@link Map}. The query parameters are
     * URI-encoded, and we should decode them when populating the map. In case we have several
     * parameters with the same name, the last one wins.
     */
    private Map<String, String> getQueryParameters(URI uri) {
        return toMap(uri.getRawQuery());
    }

    private Map<String, String> toMap(String source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return Stream.of(source.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(p -> decodeUrlPart(p[0]), p -> decodeUrlPart(p[1]), (first, second) -> second));
    }

    private static String decodeUrlPart(String encodedPart) {
        return URLDecoder.decode(encodedPart, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "HttpRequest{" + "method='" + method + '\'' +
        		", path=" + path +
                ", uri=" + uri +
                ", queryParameters=" + queryParameters +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", headers=" + headers.entrySet() +
                ", body='" + body + '\'' +
                '}';
    }
}
