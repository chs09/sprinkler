package de.operatorplease.sprinkler.http;

import java.io.File;
import java.util.Objects;

public class HttpFileHandler implements HttpHandler {
	
	final File WEB_ROOT;
	final String DEFAULT_FILE;

	public HttpFileHandler(File webRoot) {
		this.WEB_ROOT = Objects.requireNonNull(webRoot, "web-root not specified");
		this.DEFAULT_FILE = "index.html";
	}
	
	public HttpFileHandler(File webRoot, String defaultFile) {
		this.WEB_ROOT = Objects.requireNonNull(webRoot, "web-root not specified");
		this.DEFAULT_FILE = defaultFile;
	}
	
	@Override
	public void handle(HttpRequest request, HttpResponse response) throws Exception {
		String method = request.getMethod();
		
		// we support only GET and HEAD methods, we check
		if (!method.equals("GET")  &&  !method.equals("HEAD")) {
			System.out.println("501 Not Implemented : " + method + " method.");
			response.setStatusCode(501);
			return;
		}
		
		String path = request.getPath();
		if(path.contains("..")) {
			// bad request, prevent web root path escape
			response.setStatusCode(400);
			return;
		}
		
		if (path.endsWith("/") || path.isEmpty()) {
			if(DEFAULT_FILE == null) {
				response.setStatusCode(404);
				return;
			}
			
			path += DEFAULT_FILE;
		}
		
		File file = new File(WEB_ROOT, path);
		if(!file.exists()) {
			System.out.println("File " + path + " not found");
			response.setStatusCode(404);
			return;
		}
		
		response.setBody(file);
		System.out.println("File " + path + " requested, contentType " + response.getContentType());
	}
}
