package de.operatorplease.sprinkler.http;

public class HttpExceptionNotFound extends HttpException {
	private static final long serialVersionUID = 142703456610980210L;

	public HttpExceptionNotFound() {
		super(404, "Not Found");
	}
	
	public HttpExceptionNotFound(String message) {
		super(404, message);
	}
}
