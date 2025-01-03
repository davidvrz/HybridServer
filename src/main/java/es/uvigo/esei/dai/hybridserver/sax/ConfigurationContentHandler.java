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

    // Flags para detectar qué campo estamos procesando
    private boolean isHttp, isWebservice, isNumClients, isUser, isPassword, isUrl;

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public void startDocument() throws SAXException {
        // Inicializar la lista de configuraciones cuando empieza el análisis del documento
        configurations = new LinkedList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (localName) {
            case "configuration":
                configuration = new Configuration();
                break;
            case "connections":
                // No hacer nada por ahora
                break;
            case "http":
                isHttp = true;
                break;
            case "webservice":
                isWebservice = true;
                break;
            case "numClients":
                isNumClients = true;
                break;
            case "database":
                // No hacer nada por ahora
                break;
            case "user":
                isUser = true;
                break;
            case "password":
                isPassword = true;
                break;
            case "url":
                isUrl = true;
                break;
            case "servers":
                serverConfigs = new LinkedList<>();
                break;
            case "server":
                // Crear un objeto ServerConfiguration con los atributos del servidor
                ServerConfiguration serverConfig = new ServerConfiguration(
                    attributes.getValue("name"),
                    attributes.getValue("wsdl"),
                    attributes.getValue("namespace"),
                    attributes.getValue("service"),
                    attributes.getValue("httpAddress")
                );
                serverConfigs.add(serverConfig);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String textContent = new String(ch, start, length).trim();
        if (!textContent.isEmpty()) {
            try {
                if (isHttp) {
                    // Validación del puerto HTTP
                    int httpPort = Integer.parseInt(textContent);
                    if (httpPort <= 0) {
                        throw new SAXException("El parámetro 'http' debe ser un número mayor que 0.");
                    }
                    configuration.setHttpPort(httpPort);
                    isHttp = false;
                } else if (isWebservice) {
                    // Asignar la URL del webservice
                    configuration.setWebServiceURL(textContent);
                    isWebservice = false;
                } else if (isNumClients) {
                    // Validación del número de clientes
                    int numClients = Integer.parseInt(textContent);
                    if (numClients <= 0) {
                        throw new SAXException("El parámetro 'numClients' debe ser un número mayor que 0.");
                    }
                    configuration.setNumClients(numClients);
                    isNumClients = false;
                } else if (isUser) {
                    // Asignar el usuario de la base de datos
                    configuration.setDbUser(textContent);
                    isUser = false;
                } else if (isPassword) {
                    // Asignar la contraseña de la base de datos
                    configuration.setDbPassword(textContent);
                    isPassword = false;
                } else if (isUrl) {
                    // Asignar la URL de la base de datos
                    configuration.setDbURL(textContent);
                    isUrl = false;
                }
            } catch (NumberFormatException e) {
                throw new SAXException("Valor inválido: " + textContent, e);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (localName) {
            case "configuration":
                configurations.add(configuration); // Al finalizar una configuración, la agregamos a la lista
                break;
            case "servers":
                configuration.setServers(serverConfigs); // Agregar servidores a la configuración
                break;
        }
    }
    
	public Configuration getConfig() {
		return configuration;
	}
}
