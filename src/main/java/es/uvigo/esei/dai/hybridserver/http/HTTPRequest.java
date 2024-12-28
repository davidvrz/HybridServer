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
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPRequest {
	private HTTPRequestMethod method;
	private String resourceChain, resourceName;
	private String[] resourcePath;
	private String httpVersion;
	private String content;
	private int contentLength;
	private LinkedHashMap<String, String> headers;
	private LinkedHashMap<String, String> parameters;

	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {
		BufferedReader bufferedReader = new BufferedReader(reader);
		this.headers = new LinkedHashMap<>();
		this.parameters = new LinkedHashMap<>();
		  
		// METHOD, RESOURCECHAIN Y HTTPVERSION
	    String requestLine = bufferedReader.readLine();
	    System.out.println("requestLine: " + requestLine);
	    if (requestLine == null || requestLine.isEmpty()) {
	      throw new HTTPParseException("La solicitud está vacía.");
	    }
	
	    String[] requestParts = requestLine.split(" ");
	    
	    if (requestParts.length != 3) {
	      throw new HTTPParseException("Formato de solicitud HTTP inválido.");
	    }
	
	    // METHOD
	    
	    if (requestParts[0].isEmpty()) {
	        throw new HTTPParseException("Método HTTP faltante o no soportado en la solicitud");
	    }
	    try {
	    	this.method = HTTPRequestMethod.valueOf(requestParts[0]);
		} catch (IllegalArgumentException e) {
		    throw new HTTPParseException("Método HTTP no soportado: " + requestParts[0]);
		}
	    
	    // RESOURCECHAIN
	    if (requestParts[1].isEmpty() || requestParts[1].contains("HTTP/")) {
	        throw new HTTPParseException("Recurso faltante o mal formado en la solicitud");
	    }
	    this.resourceChain = requestParts[1];

	    // HTTPVERSION
	    if (!requestParts[2].startsWith("HTTP/")) {
	        throw new HTTPParseException("Versión HTTP faltante o incorrecta");
	    }
	    this.httpVersion = requestParts[2];
	
	   
	    // RESOURCEPATH, RESOURCENAME Y RESOURCEPARAMETERS
	    if (this.resourceChain.contains("?")) {
	    	String[] resourceChainSplit = this.resourceChain.split("\\?");
	    	this.resourceName = resourceChainSplit[0].substring(1);
	    	this.resourcePath = resourceChainSplit[0].substring(1).split("/");
	    	
	    	if (resourceChainSplit.length > 1) {
				String[] paramPairs = resourceChainSplit[1].split("&");
				for (String pair : paramPairs) {
					if (!pair.contains("="))
						throw new HTTPParseException("Invalid parameters");
					
					String[] keyValue = pair.split("=", 2);
					if (keyValue.length == 2) {
						parameters.put(keyValue[0], keyValue[1]);	
					}
				}
	    	}
	    } else {
			if (this.getResourceChain().equals("/")) {
				this.resourcePath = new String[0];
				this.resourceName = "";
			} else {
				this.resourcePath = this.resourceChain.substring(1).split("/");				
				this.resourceName = this.resourceChain.substring(1);
				
			}
			
		}
	    
	    // HEADERS
	    while ((requestLine = bufferedReader.readLine()) != null && requestLine.contains(": ")) {
            String[] headerPairs = requestLine.split(": ");
            if (headerPairs.length == 2) {
                this.headers.put(headerPairs[0], headerPairs[1]);
            }
        }
	    if (requestLine != null && !requestLine.isEmpty()) {
			throw new HTTPParseException("Invalid header" + requestLine);
		}
	    
	    // CONTENT
	    
        if (this.headers.containsKey(HTTPHeaders.CONTENT_LENGTH.getHeader())) {
        	this.contentLength = Integer.parseInt(this.headers.get(HTTPHeaders.CONTENT_LENGTH.getHeader()));
    	    char[] contentBuffer = new char[contentLength];
    	    bufferedReader.read(contentBuffer, 0, contentLength);
    	    this.content = new String(contentBuffer);
        }
        
	    String type = headers.get(HTTPHeaders.CONTENT_TYPE.getHeader());
        if (type != null && type.startsWith(MIME.FORM.getMime())) {
        	this.content = URLDecoder.decode(content, "UTF-8");
        }
        
        if(this.method == HTTPRequestMethod.POST) {
            String[] parts = this.content.split("&");
            for (String part : parts) {
            	if (!part.contains("="))
					throw new HTTPParseException("Invalid parameters");
                String[] keyValue = part.split("=", 2);
                if (keyValue.length == 2) {
                    this.parameters.put(keyValue[0], keyValue[1]); 
                }
            }
        }
      
	}

	public HTTPRequestMethod getMethod() {
		return this.method;
    }

    public String getResourceChain() {
    	return this.resourceChain;
    }
	  
	public String[] getResourcePath() {
		return this.resourcePath;
	}

    public String getResourceName() {
        return this.resourceName;
    }

	public Map<String, String> getResourceParameters() {
		return this.parameters;
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
		return this.contentLength;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder().append(this.getMethod().name()).append(' ').append(this.getResourceChain()).append(' ').append(this.getHttpVersion()).append("\r\n");

		for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
			sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
		}

		if (this.getContentLength() > 0) {
			sb.append("\r\n").append(this.getContent());
		}

		return sb.toString();
	}
}