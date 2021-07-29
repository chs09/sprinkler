package de.operatorplease.sprinkler.web;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.operatorplease.sprinkler.Controller;
import de.operatorplease.sprinkler.http.HttpExceptionBadRequest;
import de.operatorplease.sprinkler.http.HttpExceptionMethodNotAllowed;
import de.operatorplease.sprinkler.http.HttpExceptionNotFound;
import de.operatorplease.sprinkler.http.HttpHandler;
import de.operatorplease.sprinkler.http.HttpRequest;
import de.operatorplease.sprinkler.http.HttpRequest.Method;
import de.operatorplease.sprinkler.web.handler.PlanHandler;
import de.operatorplease.sprinkler.web.handler.ZoneHandler;
import de.operatorplease.sprinkler.http.HttpResponse;

class RestAPI implements HttpHandler {
	
	public static final String BASE = "/api";
	public static final Pattern pattern = Pattern.compile("(\\w+)/([_a-zA-Z0-9]+)/(\\w+)/?");
	
	private final Map<String, RestHandler<?>> handler;
	
	public RestAPI(Controller controller) {
		this.handler = Map.of(
			"valve", new ZoneHandler(controller),
			"plan", new PlanHandler(controller)
		);
	}
	
	private final ObjectMapper mapper= new ObjectMapper();
	{
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response) throws Exception {
		final Matcher matcher = pattern.matcher(request.getPath());
		
		if(!matcher.matches()) {
			throw new HttpExceptionNotFound("Invalid api uri '" + request.getPath() + "'");
		}
		
		final String what = matcher.group(1);
		final String id = matcher.group(2);
		final String command = matcher.group(3);
		
		final RestHandler<?> handler = this.handler.get(what);
		if(handler == null) {
			throw new HttpExceptionBadRequest("Unknown object identifier '" + what + "'");
		}
		
		Object result;
		Method method = request.getMethod();
		switch(method) {
			case GET:
				result = handler.handleGet(command, id);
				break;
				
			case POST:
				result = handler.handlePost(command, id);
				break;
				
			case PUT:
				result = handlePut(handler, command, id, request.getBody());
				break;
				
			case DELETE:
				result = handler.handleDelete(command, id);
				break;
				
			default:
				throw new HttpExceptionMethodNotAllowed(method + " not allowed");
		}
		
		if(result == null) {
			response.setStatusCode(202);
		} else {
			response.setStatusCode(200);
			if(result instanceof String) {
				response.setBody((String) result);
			} else {
				response.setBody(mapper.writeValueAsString(result));
			}
		}
	}
	
	private <T> Object handlePut(RestHandler<T> handler, String command, String id, String requestBody) {
		T body;
		if(Void.class.equals(handler.getType())) {
			body = null;
		} else if(String.class.equals(handler.getType())) {
			body = handler.getType().cast( requestBody );
		} else {
			try {
				body = requestBody == null ? null : mapper.readValue(requestBody, handler.getType());
			} catch (JsonProcessingException e) {
				throw new HttpExceptionBadRequest(e.getMessage());
			}
		}
		return handler.handlePut(command, id, body);
	}
}
