package es.uvigo.esei.dai.hybridserver.webservice;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Service;


public class WebServiceConnection {

	private String name, wsdl, namespace, service, httpAddress;

	public WebServiceConnection(String name, String wsdl, String namespace, String service, String httpAddress) {
		this.name = name;
		this.wsdl = wsdl;
		this.namespace = namespace;
		this.service = service;
		this.httpAddress = httpAddress;
		
	}

	public WebServiceInterface setConnection() {
		WebServiceInterface ws= null;
		try {
			URL url = new URL(wsdl);
			QName name = new QName(namespace, service);

			Service servicio = Service.create(url, name);
			ws = servicio.getPort(WebServiceInterface.class);

		} catch (MalformedURLException e) {
			e.getMessage();
		}
		return ws;
	}

}
