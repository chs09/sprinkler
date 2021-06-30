package de.operatorplease.sprinkler.web;

import de.operatorplease.sprinkler.http.HttpHandler;
import de.operatorplease.sprinkler.http.HttpRequest;
import de.operatorplease.sprinkler.http.HttpResponse;

public class Rest implements HttpHandler {
	
	@Override
	public void handle(HttpRequest request, HttpResponse response) throws Exception {
		if("GET".equals(request.getMethod())) {
			
		}
	}

}
