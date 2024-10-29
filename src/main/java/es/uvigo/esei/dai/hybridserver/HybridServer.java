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

import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.model.PagesDBDAO;

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
	    initializePages(); 
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
					try {
						Socket clientSocket = serverSocket.accept();
						if (stop)
							break;

						new Thread(new HybridServerThread(clientSocket)).start();
					} catch (IOException e) {
                        e.printStackTrace();
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

	public static void initializePages() {
		PagesDBDAO pagesDBDAO = new PagesDBDAO();
		
		try {
			if (!pagesDBDAO.hasPages()) {
		        pagesDBDAO.savePage("<html><body><h1>Bienvenida a Hybrid Server</h1><p>Este es el servidor que sirve páginas HTML.</p></body></html>");
		        pagesDBDAO.savePage("<html><body><h1>Acerca de</h1><p>Este servidor fue creado para servir páginas almacenadas en una base de datos.</p></body></html>");
			}
		} catch (JDBCException e) {
			System.err.println("Error al inicializar las páginas: " + e.getMessage());
		}
  
	}	
  
  
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