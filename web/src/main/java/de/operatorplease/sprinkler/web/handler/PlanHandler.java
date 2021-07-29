package de.operatorplease.sprinkler.web.handler;

import de.operatorplease.sprinkler.Controller;
import de.operatorplease.sprinkler.http.HttpExceptionBadRequest;
import de.operatorplease.sprinkler.http.HttpExceptionNotFound;
import de.operatorplease.sprinkler.http.HttpExceptionNotImplemented;
import de.operatorplease.sprinkler.settings.Plan;
import de.operatorplease.sprinkler.web.RestHandler;

public class PlanHandler extends RestHandler<Plan> {
	private Controller controller;
	
	public PlanHandler(Controller controller) {
		this.controller = controller;
	}
	
	@Override
	public Class<Plan> getType() {
		return Plan.class;
	}
	
	private Plan getPlan(String id) {
		for(Plan s: controller.getPlans()) {
			if(Integer.toString(s.getPid()) == id) {
				return s;
			}
		}
		throw new HttpExceptionNotFound("Plan '" + id + "' not found");
	}
	
	@Override
	public Object handleGet(String command, String id) {
		if("_all".equals(id)) {
			if("status".equals(command)) {
				return controller.getPlans();
			}				
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command for pseudo id _all");
		}
		
		Plan plan = getPlan(id);
		if("status".equals(command)) {
			return plan;
		} else {
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command for pseudo id _all");
		}
	}
	
	@Override
	public Object handlePut(String command, String id, Plan plan) {
		if(!Integer.toString(plan.getPid()).equals(id)) {
			throw new HttpExceptionBadRequest();
		}
		
		switch(command) {
		case "update":
			System.out.println("update plan " + id);
			//station.start();
			return null;

		default:
			throw new HttpExceptionNotImplemented("'" + command + "' does not match any known command");
		}
	}
}