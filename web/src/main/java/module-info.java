module de.operatorplease.sprinkler.web {
	requires java.logging;
	requires de.operatorplease.sprinkler.controller;
	requires jdk.httpserver;
	requires com.fasterxml.jackson.databind;
	
	exports de.operatorplease.sprinkler.web;
}