package de.operatorplease.sprinkler.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.operatorplease.sprinkler.Controller;
import de.operatorplease.sprinkler.Station;
import de.operatorplease.sprinkler.http.HttpExceptionBadRequest;
import de.operatorplease.sprinkler.http.HttpExceptionMethodNotAllowed;
import de.operatorplease.sprinkler.http.HttpExceptionNotFound;
import de.operatorplease.sprinkler.http.HttpExceptionNotImplemented;
import de.operatorplease.sprinkler.http.HttpHandler;
import de.operatorplease.sprinkler.http.HttpRequest;
import de.operatorplease.sprinkler.http.HttpRequest.Method;
import de.operatorplease.sprinkler.web.dto.ValveDTO;
import de.operatorplease.sprinkler.http.HttpResponse;

class RestAPI implements HttpHandler {
	
	public static final String BASE = "/api";
	public static final Pattern pattern = Pattern.compile("(\\w+)/([_a-zA-Z0-9]+)/(\\w+)/?");
	
	private final Controller controller;
	
	public RestAPI(Controller controller) {
		this.controller = controller;
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
		
		Object result;
		Method method = request.getMethod();		
		switch(what) {
			case "valve":
				result = handleValve(method, id, command);
				break;
				
			default:
				throw new HttpExceptionBadRequest("Unknown object identifier '" + what + "'");
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

	private Object handleValve(Method method, String id, String command) {
		if("_all".equals(id)) {
			if("stop".equals(command)) {
				controller.stopAllZonesImmediate();
				return null;
			}
			
			if("status".equals(command)) {
				return controller.getStations().stream().map(ValveDTO::new).collect(Collectors.toList());
			}
			
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command for pseudo id _all");
		}

		Station station = null;
		for(Station s: controller.getStations()) {
			if(s.getZoneId().equals(id)) {
				station = s;
				break;
			}
		}
		
		if(station == null) {
			throw new HttpExceptionNotFound("Valve '" + id + "' not found");
		}
		
		if(method == Method.POST) {
			switch(command) {
			case "open":
				System.out.println("open value " + id);
				station.start();
				break;
					
			case "close":
				System.out.println("close value " + id);
				station.stop();
				break;
				
			case "status":
				return new ValveDTO(station);
				
			default:
				throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command");
			}
			return null;
		}
		throw new HttpExceptionMethodNotAllowed();
	}
}
