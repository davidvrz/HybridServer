package es.uvigo.esei.dai.hybridserver.webservice;

import java.util.ArrayList;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.ServerConfiguration;
import jakarta.xml.ws.WebServiceException;

public class DocumentServiceUtils {

	public static List<ServerConnection> getConnections(List<ServerConfiguration> listServers) {
	    List<ServerConnection> serverConnections = new ArrayList<>();

	    for (ServerConfiguration serverConfiguration : listServers) {
	        try {
	            DocumentServiceConnection wsc = new DocumentServiceConnection(
	                serverConfiguration.getName(),
	                serverConfiguration.getWsdl(),
	                serverConfiguration.getNamespace(),
	                serverConfiguration.getService(),
	                serverConfiguration.getHttpAddress()
	            );

	            DocumentService connection = wsc.setConnection();
	            if (connection != null) {
	                serverConnections.add(new ServerConnection(serverConfiguration, connection));
	            }
	        } catch (WebServiceException e) {
	            System.out.println("No se pudo conectar al servidor: " + serverConfiguration.getName());
	            e.printStackTrace(); 
	        }
	    }

	    return serverConnections;
	}

}
