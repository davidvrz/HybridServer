package es.uvigo.esei.dai.hybridserver.sax;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import es.uvigo.esei.dai.hybridserver.Configuration;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

public class ConfigurationContentHandler extends DefaultHandler {

	private List<Configuration> configurations;
	private Configuration configuration;
	
	private List<ServerConfiguration> serverConfigs;

	private boolean http, webservice, numClients, user, password, url;

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void startDocument() throws SAXException {
		this.configurations = new LinkedList<>();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if ("configuration".equals(localName)) {
			this.configuration = new Configuration();
			
		} else if ("connections".equals(localName)) {

		} else if ("http".equals(localName)) {
			http = true;
			
		} else if ("webservice".equals(localName)) {
			webservice = true;
			
		} else if ("numClients".equals(localName)) {
			numClients = true;
			
		} else if ("database".equals(localName)) {
			
		} else if ("user".equals(localName)) {
			user = true;
			
		} else if ("password".equals(localName)) {
			password = true;
			
		} else if ("url".equals(localName)) {
			url = true;
			
		} else if ("servers".equals(localName)) {
			this.serverConfigs = new LinkedList<>();
			
		} else if ("server".equals(localName)) {
			ServerConfiguration serverConfig = new ServerConfiguration(
				attributes.getValue("name"),
				attributes.getValue("wsdl"),
				attributes.getValue("namespace"),
				attributes.getValue("service"),
				attributes.getValue("httpAddress")
			);
			serverConfigs.add(serverConfig);
			
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		String textContent = new String(ch, start, length);
		if (!textContent.trim().isEmpty()) {
			
			if (http) {
				this.configuration.setHttpPort(Integer.parseInt(textContent));
				
			} else if (webservice) {
				this.configuration.setWebServiceURL(textContent);
				
			} else if (numClients) {
				this.configuration.setNumClients(Integer.parseInt(textContent));
				
			} else if (user) {
				this.configuration.setDbUser(textContent);
				
			} else if (password) {
				this.configuration.setDbPassword(textContent);
				
			} else if (url) {
				this.configuration.setDbURL(textContent);
				
			}
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if ("configuration".equals(localName)) {

		} else if ("connections".equals(localName)) {
			
		} else if ("http".equals(localName)) {
			http = false;
			
		} else if ("webservice".equals(localName)) {
			webservice = false;
			
		} else if ("numClients".equals(localName)) {
			numClients = false;
			
		} else if ("database".equals(localName)) {
			
		} else if ("user".equals(localName)) {
			user = false;
			
		} else if ("password".equals(localName)) {
			password = false;
			
		} else if ("url".equals(localName)) {
			url = false;
			
		} else if ("servers".equals(localName)) {
			this.configuration.setServers(serverConfigs);
			
		} else if ("server".equals(localName)) {
			
		}
	}
	
	public Configuration getConfig() {
		return configuration;
	}

}
