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

import es.uvigo.esei.dai.hybridserver.controller.HTMLController;
import es.uvigo.esei.dai.hybridserver.controller.XMLController;
import es.uvigo.esei.dai.hybridserver.controller.XSDController;
import es.uvigo.esei.dai.hybridserver.controller.XSLTController;
import es.uvigo.esei.dai.hybridserver.model.HTMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSDDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSLTDBDAO;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerServiceImpl;

import jakarta.xml.ws.Endpoint;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
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
  
	private HTMLController htmlController;
	private XMLController xmlController;
	private XSDController xsdController;
	private XSLTController xsltController;

	private Configuration configuration=null;
	private Endpoint endPoint=null;
	List<ServerConfiguration> listServers=null;
	
    private HybridServerServiceImpl hybridServerServiceImpl;

    public HybridServer() {
        this.servicePort = 8888;
        this.maxClients = 50;
        dbUrl = "jdbc:mysql://localhost:3306/hstestdb";
        dbUser = "hsdb";
        dbPassword = "hsdbpass";
        
        htmlController = new HTMLController(new HTMLDBDAO(dbUrl, dbUser, dbPassword), listServers);
        xmlController = new XMLController(new XMLDBDAO(dbUrl, dbUser, dbPassword), new XSDDBDAO(dbUrl, dbUser, dbPassword), new XSLTDBDAO(dbUrl, dbUser, dbPassword),listServers);
        xsdController = new XSDController(new XSDDBDAO(dbUrl, dbUser, dbPassword),listServers);
        xsltController = new XSLTController(new XSLTDBDAO(dbUrl, dbUser, dbPassword), new XSDDBDAO(dbUrl, dbUser, dbPassword),listServers);
    }
	
	public HybridServer(Properties properties) {
	    this.servicePort = Integer.parseInt(properties.getProperty("port"));
        this.maxClients = Integer.parseInt(properties.getProperty("numClients"));
        dbUrl = properties.getProperty("db.url");
        dbUser = properties.getProperty("db.user");
        dbPassword = properties.getProperty("db.password");
        
        htmlController = new HTMLController(new HTMLDBDAO(dbUrl, dbUser, dbPassword), listServers);
        xmlController = new XMLController(new XMLDBDAO(dbUrl, dbUser, dbPassword), new XSDDBDAO(dbUrl, dbUser, dbPassword), new XSLTDBDAO(dbUrl, dbUser, dbPassword), listServers);
        xsdController = new XSDController(new XSDDBDAO(dbUrl, dbUser, dbPassword), listServers);
        xsltController = new XSLTController(new XSLTDBDAO(dbUrl, dbUser, dbPassword), new XSDDBDAO(dbUrl, dbUser, dbPassword), listServers);
	}
	
	public HybridServer(Configuration configuration) {
		this.configuration = configuration;
		
		this.servicePort = configuration.getHttpPort();
		this.maxClients = configuration.getNumClients();
		dbUrl = configuration.getDbURL();
		dbUser = configuration.getDbUser();
		dbPassword = configuration.getDbPassword();
		List<ServerConfiguration> listServers = configuration.getServers();

		htmlController = new HTMLController(new HTMLDBDAO(dbUrl, dbUser, dbPassword), listServers);
		xmlController = new XMLController(new XMLDBDAO(dbUrl, dbUser, dbPassword), new XSDDBDAO(dbUrl, dbUser, dbPassword), new XSLTDBDAO(dbUrl, dbUser, dbPassword), listServers);
		xsdController = new XSDController(new XSDDBDAO(dbUrl, dbUser, dbPassword), listServers);
		xsltController = new XSLTController(new XSLTDBDAO(dbUrl, dbUser, dbPassword), new XSDDBDAO(dbUrl, dbUser, dbPassword), listServers);

	}
	
	public int getPort() {
		return this.servicePort;
	}
	
	public void start() {
		this.serverThread = new Thread() {
		@Override
		public void run() {
			try (final ServerSocket serverSocket = new ServerSocket(servicePort)) {
				threadPool = Executors.newFixedThreadPool(maxClients);
				try {
					String url = configuration.getWebServiceURL();
					hybridServerServiceImpl = new HybridServerServiceImpl(dbUrl, dbUser, dbPassword);
					endPoint = Endpoint.publish(url, hybridServerServiceImpl);
					endPoint.setExecutor(threadPool);
				}catch(IllegalArgumentException | NullPointerException ex) {
					ex.printStackTrace();
				}
				while (true) {
					try {
						Socket clientSocket = serverSocket.accept();
						if (stop)
							break;
						
							threadPool.execute(new HybridServerThread(clientSocket, htmlController, xmlController, xsdController, xsltController));
					} catch (IOException e) {
						
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
  
  
  	@Override
  	public void close() {
  	    this.stop = true;

  	    if (endPoint != null) {
  	    	try {
              	System.out.println("Deteniendo servicio web...");
              	endPoint.stop(); 
              	endPoint = null;  
          	} catch (Exception e) {
              	e.printStackTrace();
          	}
  	    }
  	    
	  	try {
	        Thread.sleep(500);  // Pausa para permitir que se cierre el servicio antes de intentar crear otro en la misma url
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }

  	    // "Despertar" el hilo servidor para que finalice
  	    try (Socket socket = new Socket("localhost", servicePort)) {
  	    } catch (IOException e) {
  	        throw new RuntimeException(e);
  	    }

  	    try {
  	        this.serverThread.join();
  	    } catch (InterruptedException e) {
  	        throw new RuntimeException(e);
  	    }

  	    // Detener el pool de hilos
  	    threadPool.shutdownNow();
  	    try {
  	        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
  	    } catch (InterruptedException e) {
  	        e.printStackTrace();
  	    }
  	}

}

