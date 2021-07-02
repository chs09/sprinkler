package de.operatorplease.sprinkler.http;

abstract class HttpException extends RuntimeException {
	private static final long serialVersionUID = 142703456610980210L;

	private final int status;
	
	public HttpException(int status, String message) {
		super(message);
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
}
