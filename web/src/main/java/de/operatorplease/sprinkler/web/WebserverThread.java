package de.operatorplease.sprinkler.web;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import de.operatorplease.sprinkler.Controller;
import de.operatorplease.sprinkler.http.HttpFileHandler;
import de.operatorplease.sprinkler.http.HttpServer;

public class WebserverThread extends Thread {
	
	private final Logger logger = Logger.getLogger(WebserverThread.class.getSimpleName());
	
//	private final Controller controller;
	private final HttpServer server;
	private final int port;
	
	public WebserverThread(@SuppressWarnings("exports") Controller controller, int port) {
//		this.controller = controller;
		this.server = new HttpServer();
		this.port = port;
		
		server.addHandler(RestAPI.BASE, new RestAPI(controller));
		server.addHandler("/", new HttpFileHandler(new File("./"), "app.html"));
	}
	
	public void run() {
		server.start(port);
		try {
			while(true) {
				logger.fine("Webserver is running...");
				Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
			}
        } catch (InterruptedException e) {
        	logger.warning("Interrupted - shutting down ...");
            Thread.currentThread().interrupt();
        }
	}
}
