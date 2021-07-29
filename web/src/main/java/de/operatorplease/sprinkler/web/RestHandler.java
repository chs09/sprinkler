package de.operatorplease.sprinkler.web;

import de.operatorplease.sprinkler.http.HttpExceptionMethodNotAllowed;

public abstract class RestHandler<T> {
	
	public abstract Class<T> getType();
	
	public Object handlePost(String command, String id) {
		throw new HttpExceptionMethodNotAllowed();
	}
	
	public Object handlePut(String command, String id, T body) {
		throw new HttpExceptionMethodNotAllowed();
	}

	public Object handleGet(String command, String id) {
		throw new HttpExceptionMethodNotAllowed();
	}
	
	public Object handleDelete(String command, String id) {
		throw new HttpExceptionMethodNotAllowed();
	}
}