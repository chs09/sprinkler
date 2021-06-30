package demo;

import java.io.File;
import java.util.concurrent.TimeUnit;

import de.operatorplease.sprinkler.http.HttpFileHandler;
import de.operatorplease.sprinkler.http.HttpServer;

public class Demo {
	public static void main(String[] args) {
		HttpServer server = new HttpServer();
		server.addHandler("/", new HttpFileHandler(new File("./"), "app.html"));
		server.start(80);
		
		try {
			while(true) {
				System.out.println("Webserver is running...");
				Thread.sleep(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES));
			}
        } catch (InterruptedException e) {
        	System.out.println("Shutting down ...");
            Thread.currentThread().interrupt();
            System.exit(0);
        }
	}
}
