package de.operatorplease.sprinkler.http;

public class HttpExceptionBadRequest extends HttpException {
	private static final long serialVersionUID = 142703456610980210L;

	public HttpExceptionBadRequest() {
		super(400, "Bad Request");
	}
	
	public HttpExceptionBadRequest(String message) {
		super(400, message);
	}
}
