module de.operatorplease.sprinkler.controller {
	requires java.logging;
	requires org.shredzone.commons.suncalc;
	requires com.fasterxml.jackson.databind;
	
	exports de.operatorplease.sprinkler;
	exports de.operatorplease.sprinkler.settings;
	exports de.operatorplease.sprinkler.weather;
}