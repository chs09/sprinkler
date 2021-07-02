package de.operatorplease.sprinkler.http;

public class HttpExceptionMethodNotAllowed extends HttpException {
	private static final long serialVersionUID = 142703456610980210L;

	public HttpExceptionMethodNotAllowed() {
		super(405, "Method Not Allowed");
	}
	
	public HttpExceptionMethodNotAllowed(String message) {
		super(405, message);
	}
}
