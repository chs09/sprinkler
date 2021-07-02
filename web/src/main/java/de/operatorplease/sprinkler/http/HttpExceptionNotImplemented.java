package de.operatorplease.sprinkler.http;

public class HttpExceptionNotImplemented extends HttpException {
	private static final long serialVersionUID = 142703456610980210L;

	public HttpExceptionNotImplemented() {
		super(501, "Not Implemented");
	}
	
	public HttpExceptionNotImplemented(String message) {
		super(501, message);
	}
}
