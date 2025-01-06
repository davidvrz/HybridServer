package es.uvigo.esei.dai.hybridserver.webservice;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Service;


public class HybridServerServiceConnection {

	private String name, wsdl, namespace, service, httpAddress;

	public HybridServerServiceConnection(String name, String wsdl, String namespace, String service, String httpAddress) {
		this.name = name;
		this.wsdl = wsdl;
		this.namespace = namespace;
		this.service = service;
		this.httpAddress = httpAddress;
		
	}

	public HybridServerService setConnection() {
		HybridServerService hs= null;
		try {
			URL url = new URL(wsdl);
			QName name = new QName(namespace, service);

			Service service = Service.create(url, name);
			hs = service.getPort(HybridServerService.class);

		} catch (MalformedURLException e) {
			 System.err.println("Error setting connection: " + e.getMessage());
		}
		return hs;
	}

}
