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
package es.uvigo.esei.dai.hybridserver;

import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class HybridServer implements AutoCloseable {
  private static final int SERVICE_PORT = 2000;
  private Thread serverThread;
  private boolean stop;
  private Map<String, String> pages; 


  public HybridServer() {
	  this.pages = new HashMap<>();
      //initializeDefaultPages(); 
  }

  public HybridServer(Map<String, String> pages) {
      this.pages = pages;
  }

  public HybridServer(Properties properties) {
	  this.pages = new HashMap<>();
      for (String key : properties.stringPropertyNames()) {
          this.pages.put(key, properties.getProperty(key));
      }

  }

  public int getPort() {
    return SERVICE_PORT;
  }

  public void start() {
    this.serverThread = new Thread() {
      @Override
      public void run() {
        try (final ServerSocket serverSocket = new ServerSocket(SERVICE_PORT)) {
          while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
              if (stop)
                break;

              new Thread(new HybridServerThread(clientSocket)).start();
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };

    this.stop = false;
    this.serverThread.start();
  }
/*
  private void handleRequest(Socket socket) {
	  try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			  PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

		  	// Leer la solicitud HTTP
		    HTTPRequest request = new HTTPRequest(reader);
		    HTTPResponse response = new HTTPResponse();
		
		    // Configurar la respuesta
		    String resourceName = request.getResourceChain();
		    if (pages.containsKey(resourceName)) {
		        response.setStatus(HTTPResponseStatus.S200);
		        response.setContent(pages.get(resourceName));
		    } else {
		        response.setStatus(HTTPResponseStatus.S404);
		        response.setContent("404 Not Found");
		    }
		
		    // Escribir la respuesta HTTP
		    response.print(writer);
	
	} catch (IOException | HTTPParseException e) {
	    e.printStackTrace();
	    // Responder con un error interno en caso de excepción
	    try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
	        HTTPResponse errorResponse = new HTTPResponse();
	        errorResponse.setStatus(HTTPResponseStatus.S500);
	        errorResponse.setContent("500 Internal Server Error");
	        errorResponse.print(writer);
	    } catch (IOException ioException) {
	        ioException.printStackTrace();
	    }
	        }
	    }

  private void initializeDefaultPages() {
      // Inicializar algunas páginas predeterminadas
	  pages.put("/", "<html><body><h1>Hybrid Server</h1><p>David Álvarez Iglesias\nAntonio Caride Pernas</p></body></html>");
      pages.put("/about", "<html><body><h1>Acerca de</h1><p>Este es un servidor HTTP simple construido en Java.</p></body></html>");
  }
  
*/  
  @Override
  public void close() {
    this.stop = true;

    try (Socket socket = new Socket("localhost", SERVICE_PORT)) {
      // Esta conexión se hace, simplemente, para "despertar" el hilo servidor
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      this.serverThread.join();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    this.serverThread = null;
  }
}