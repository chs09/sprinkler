package de.operatorplease.sprinkler.web.handler;

import java.util.stream.Collectors;

import de.operatorplease.sprinkler.Controller;
import de.operatorplease.sprinkler.Station;
import de.operatorplease.sprinkler.http.HttpExceptionNotFound;
import de.operatorplease.sprinkler.http.HttpExceptionNotImplemented;
import de.operatorplease.sprinkler.web.RestHandler;
import de.operatorplease.sprinkler.web.dto.ValveDTO;

public class ZoneHandler extends RestHandler<Void> {
	private Controller controller;
	
	@Override
	public Class<Void> getType() {
		return Void.TYPE;
	}
	
	public ZoneHandler(Controller controller) {
		this.controller = controller;
	}
	
	private Station getStation(String id) {
		for(Station s: controller.getStations()) {
			if(s.getZoneId().equals(id)) {
				return s;
			}
		}
		throw new HttpExceptionNotFound("Zone '" + id + "' not found");
	}
	
	@Override
	public Object handleGet(String command, String id) {
		if("_all".equals(id)) {
			if("status".equals(command)) {
				return controller.getStations().stream().map(ValveDTO::new).collect(Collectors.toList());
			}				
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command for pseudo id _all");
		}
		
		Station station = getStation(id);
		if("status".equals(command)) {
			return new ValveDTO(station);
		} else {
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command for pseudo id _all");
		}
	}
	
	@Override
	public Object handlePost(String command, String id) {
		if("_all".equals(id)) {
			if("close".equals(command)) {
				controller.stopAllZonesImmediate();
				return null;
			}
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command for pseudo id _all");
		}

		Station station = getStation(id);
		switch(command) {
		case "open":
			System.out.println("open value " + id);
			station.start();
			return null;

		case "close":
			System.out.println("close value " + id);
			station.stop();
			return null;

		default:
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command");
		}
	}
}