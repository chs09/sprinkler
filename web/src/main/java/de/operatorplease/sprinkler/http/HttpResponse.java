package de.operatorplease.sprinkler.http;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.Headers;

/**
 * Represents an HTTP responses from an HTTP server
 */
public class HttpResponse {

    private int statusCode;
    private Headers headers;
    private byte[] body;
    private String contentType;

    public HttpResponse() {
        this(200, new Headers(), "");
    }

    public HttpResponse(int statusCode, Headers headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.setBody(body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public HttpResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    Headers getHeaders() {
        return headers;
    }

    public HttpResponse addHeader(String name, String value) {
        headers.add(name, value);
        return this;
    }

    public HttpResponse setHeaders(Headers headers) {
        this.headers = headers;
        return this;
    }

    public Charset getCharset() {
    	return StandardCharsets.UTF_8;
    }
    
    public byte[] getBody() {
        return body;
    }
    
    public String getContentType() {
		return contentType;
	}
    
    public void setContentType(String contentType) {
		this.contentType = contentType;
	}

    public HttpResponse setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        this.contentType = "plain/text";
        return this;
    }
    
    public HttpResponse setBody(File file) throws IOException {
    	Path path = file.toPath();
    	this.contentType = Files.probeContentType(path);
        this.body = Files.readAllBytes(path);
        
        if(contentType == null) {
        	if(file.getName().toLowerCase().endsWith(".css")) {
        		this.contentType = "text/css";
        	}
        	
        	else if(file.getName().toLowerCase().endsWith(".js")) {
        		this.contentType = "text/javascript";
        	}
        }
        
        return this;
    }

    @Override
    public String toString() {
        return "HttpResponse{" + "statusCode=" + statusCode +
                ", headers=" + headers.entrySet() +
                ", contentType='" + contentType + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
