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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {
	private HTTPResponseStatus status;
    private String version;
    private String content;
    private LinkedHashMap<String, String> parameters;
    
    public HTTPResponse() {
    	this.parameters = new LinkedHashMap<>();
        this.version = HTTPHeaders.HTTP_1_1.getHeader(); 
        this.content = "";
    }
	
    public HTTPResponseStatus getStatus() {
    	return this.status; 
  }
	
	public void setStatus(HTTPResponseStatus status) {
		this.status = status;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
        parameters.put(HTTPHeaders.CONTENT_LENGTH.getHeader(), String.valueOf(content.length()));
        parameters.put(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime() + "; charset=UTF-8");
	}

	public LinkedHashMap<String, String> getParameters() {
		return this.parameters;
	}

	public String putParameter(String name, String value) {
		return this.parameters.put(name, value);
	}

	public boolean containsParameter(String name) {
		return this.parameters.containsKey(name);
	}

	public String removeParameter(String name) {
		return this.parameters.remove(name);
	}

	public void clearParameters() {
		this.parameters.clear();
	}

	public List<String> listParameters() {
		return new ArrayList<>(this.parameters.keySet());
	}

	public void print(Writer writer) throws IOException {
		writer.write(version + " " + status.getCode() + " " + status.getStatus() + "\r\n");

        for (Map.Entry<String, String> entry : this.parameters.entrySet()) {
            writer.write(entry.getKey() + ": " + entry.getValue() + "\r\n");
        }
        writer.write("\r\n");

        if (content != null) {
            writer.write(content);
        }
    }
	
	@Override
	public String toString() {
		try (final StringWriter writer = new StringWriter()) {
			this.print(writer);
			
			return writer.toString();
		} catch (IOException e) {
			throw new RuntimeException("Unexpected I/O exception", e);
		}
	}
}