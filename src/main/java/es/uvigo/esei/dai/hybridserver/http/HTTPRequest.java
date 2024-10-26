/**
 *  HybridServer
 *  Copyright (C) 2024 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class HTTPRequest {
	private HTTPRequestMethod method;
	private String resourceChain;
	private String httpVersion;
	private Map<String, String> headers;
	private String content;

	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		this.headers = new HashMap<>();
		  
		// Leer la primera línea de la solicitud: Método, URI y versión de HTTP
	    String requestLine = bufferedReader.readLine();
	    System.out.println(requestLine);
	    if (requestLine == null || requestLine.isEmpty()) {
	      throw new HTTPParseException("La solicitud está vacía.");
	    }
	
	    String[] requestParts = requestLine.split(" ");
	    
	    if (requestParts.length != 3) {
	      throw new HTTPParseException("Formato de solicitud HTTP inválido.");
	    }
	
	    // Método HTTP
	    try {
	    	this.method = HTTPRequestMethod.valueOf(requestParts[0]);
		    } catch (IllegalArgumentException e) {
		      throw new HTTPParseException("Método HTTP no soportado: " + requestParts[0]);
		    }
	
	    // URI o cadena de recursos
	    this.resourceChain = requestParts[1];
	    System.out.println("resourceChain: " + resourceChain);
	
	    // Versión HTTP
	    this.httpVersion = requestParts[2];
	
	    // Leer las cabeceras de la solicitud
	    String line;
	    while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
	      String[] headerParts = line.split(": ");
	      if (headerParts.length == 2) {
	        this.headers.put(headerParts[0], headerParts[1]);
	      }
	    }
	
	    // Leer el contenido (si existe)
	    if (headers.containsKey("Content-Length")) {
	      int contentLength = Integer.parseInt(headers.get("Content-Length"));
	      char[] contentBuffer = new char[contentLength];
	      bufferedReader.read(contentBuffer, 0, contentLength);
	      this.content = new String(contentBuffer);
	    }
	  }

	  public HTTPRequestMethod getMethod() {
		  return this.method;
	  }

	  public String getResourceChain() {
		  return this.resourceChain;
	  }
	  /*
	  public String[] getResourcePath() {
		  return this.resourceChain.split("/");
	  }

	  public String getResourceName() {
		  String[] pathParts = getResourcePath();
		  String resourceName = pathParts[1];
		  System.out.println(pathParts[1]);

		  // Si hay parámetros en la URI, solo devolver la parte antes del "?"
		  if (resourceName.contains("?")) {
			  return resourceName.split("\\?")[0];
		  }

		  return resourceName;
	  }
	*/
	  public Map<String, String> getResourceParameters() {
		  Map<String, String> parameters = new HashMap<>();
		  if (this.resourceChain.contains("?")) {
			  String[] parts = this.resourceChain.split("\\?");
			  if (parts.length > 1) {
				  String[] paramPairs = parts[1].split("&");
				  for (String pair : paramPairs) {
					  String[] keyValue = pair.split("=");
					  if (keyValue.length == 2) {
						  parameters.put(keyValue[0], keyValue[1]);
					  }
				  }
			  }
		  }
		  return parameters;
	  }

	  public String getHttpVersion() {
		  return this.httpVersion;
	  }

	  public Map<String, String> getHeaderParameters() {
		  return this.headers;
	  }

	  public String getContent() {
		  return this.content;
	  }

	  public int getContentLength() {
		  return this.content != null ? this.content.length() : 0;
	  }

	  @Override
	  public String toString() {
	    final StringBuilder sb = new StringBuilder().append(this.getMethod().name()).append(' ')
	      .append(this.getResourceChain()).append(' ').append(this.getHttpVersion()).append("\r\n");
	
	    for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
	      sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
	    }
	
	    if (this.getContentLength() > 0) {
	      sb.append("\r\n").append(this.getContent());
	    }
	
	    return sb.toString();
	  }
}