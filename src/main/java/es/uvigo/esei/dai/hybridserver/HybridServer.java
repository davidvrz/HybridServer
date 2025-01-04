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
import es.uvigo.esei.dai.hybridserver.controller.HTMLController;
import es.uvigo.esei.dai.hybridserver.controller.XMLController;
import es.uvigo.esei.dai.hybridserver.controller.XSDController;
import es.uvigo.esei.dai.hybridserver.controller.XSLTController;
import es.uvigo.esei.dai.hybridserver.model.HTMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XMLDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSDDBDAO;
import es.uvigo.esei.dai.hybridserver.model.XSLTDBDAO;
import es.uvigo.esei.dai.hybridserver.webservice.DocumentServiceImpl;

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

    public HybridServer() {
        this.servicePort = 8888;
        this.maxClients = 50;
        dbUrl = "jdbc:mysql://localhost:3306/hstestdb";
        dbUser = "hsdb";
        dbPassword = "hsdbpass";
        
        JDBCConnection.initialize(dbUrl, dbUser, dbPassword);
        htmlController = new HTMLController(new HTMLDBDAO(), listServers);
        xmlController = new XMLController(new XMLDBDAO(), new XSDDBDAO(), new XSLTDBDAO(),listServers);
        xsdController = new XSDController(new XSDDBDAO(),listServers);
        xsltController = new XSLTController(new XSLTDBDAO(), new XSDDBDAO(),listServers);
    }
	
	public HybridServer(Properties properties) {
	    this.servicePort = Integer.parseInt(properties.getProperty("port"));
        this.maxClients = Integer.parseInt(properties.getProperty("numClients"));
        dbUrl = properties.getProperty("db.url");
        dbUser = properties.getProperty("db.user");
        dbPassword = properties.getProperty("db.password");
        
        JDBCConnection.initialize(dbUrl, dbUser, dbPassword);
        htmlController = new HTMLController(new HTMLDBDAO(), listServers);
        xmlController = new XMLController(new XMLDBDAO(), new XSDDBDAO(), new XSLTDBDAO(), listServers);
        xsdController = new XSDController(new XSDDBDAO(), listServers);
        xsltController = new XSLTController(new XSLTDBDAO(), new XSDDBDAO(), listServers);
	}
	
	public HybridServer(Configuration configuration) {
		this.configuration = configuration;
		
		this.servicePort = configuration.getHttpPort();
		this.maxClients = configuration.getNumClients();
		dbUrl = configuration.getDbURL();
		dbUser = configuration.getDbUser();
		dbPassword = configuration.getDbPassword();
		List<ServerConfiguration> listServers = configuration.getServers();

		JDBCConnection.initialize(dbUrl, dbUser, dbPassword);
		htmlController = new HTMLController(new HTMLDBDAO(), listServers);
		xmlController = new XMLController(new XMLDBDAO(), new XSDDBDAO(), new XSLTDBDAO(), listServers);
		xsdController = new XSDController(new XSDDBDAO(), listServers);
		xsltController = new XSLTController(new XSLTDBDAO(), new XSDDBDAO(), listServers);

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
					endPoint = Endpoint.publish(url, new DocumentServiceImpl());
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
	  	
		if(endPoint!=null)
			endPoint.stop();

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

