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

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class HybridServer implements AutoCloseable {
	private int servicePort;
    private int maxClients;
    String dbUrl;
    String dbUser;
    String dbPassword;
    private Thread serverThread;
    private boolean stop;
    private ExecutorService threadPool;

    public HybridServer() {
        this.servicePort = 8888;
        this.maxClients = 50;
        dbUrl = "jdbc:mysql://localhost:3306/HybridServer";
        dbUser = "hybridserver";
        dbPassword = "hspass";
        
        JDBCConnection.initialize(dbUrl, dbUser, dbPassword);
        initializePages();
        this.threadPool = Executors.newFixedThreadPool(maxClients);
    }
	
	public HybridServer(Properties properties) {
	    this.servicePort = Integer.parseInt(properties.getProperty("port", "8888"));
        this.maxClients = Integer.parseInt(properties.getProperty("numClients", "50"));
        String dbUrl = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/HybridServer");
        String dbUser = properties.getProperty("db.user", "hybridserver");
        String dbPassword = properties.getProperty("db.password", "hspass");
        
        JDBCConnection.initialize(dbUrl, dbUser, dbPassword);
        initializePages();
        this.threadPool = Executors.newFixedThreadPool(maxClients);

	}
	
	public int getPort() {
		return servicePort;
	}
	
	public void start() {
		this.serverThread = new Thread() {
		@Override
		public void run() {
			try (final ServerSocket serverSocket = new ServerSocket(servicePort)) {
				while (true) {
					try {
						Socket clientSocket = serverSocket.accept();
						if (stop)
							break;

						threadPool.execute(new HybridServerThread(clientSocket));
						
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

    	try (Socket socket = new Socket("localhost", servicePort)) {
    	// Esta conexión se hace, simplemente, para "despertar" el hilo servidor
    	} catch (IOException e) {
    		throw new RuntimeException(e);
    	}

    	try {
    		this.serverThread.join();
    	} catch (InterruptedException e) {
    		throw new RuntimeException(e);
    	}

    	threadPool.shutdownNow();

    	try {
    	  threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    	} catch (InterruptedException e) {
    	  e.printStackTrace();
    	}

  	}
}

