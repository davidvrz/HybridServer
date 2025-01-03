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

    private String currentElement;

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public void startDocument() throws SAXException {
        configurations = new LinkedList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = localName;

        if ("configuration".equals(localName)) {
            configuration = new Configuration();
        } else if ("servers".equals(localName)) {
            serverConfigs = new LinkedList<>();
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
        if (currentElement != null && !new String(ch, start, length).trim().isEmpty()) {
            String textContent = new String(ch, start, length).trim();

            switch (currentElement) {
                case "http":
                    configuration.setHttpPort(Integer.parseInt(textContent));
                    break;
                case "webservice":
                    configuration.setWebServiceURL(textContent);
                    break;
                case "numClients":
                    configuration.setNumClients(Integer.parseInt(textContent));
                    break;
                case "user":
                    configuration.setDbUser(textContent);
                    break;
                case "password":
                    configuration.setDbPassword(textContent);
                    break;
                case "url":
                    configuration.setDbURL(textContent);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("configuration".equals(localName)) {
            // Realiza las comprobaciones antes de agregar la configuración a la lista
            validateConfiguration(configuration);
            configurations.add(configuration); // Agrega la configuración completa a la lista
        } else if ("servers".equals(localName)) {
            configuration.setServers(serverConfigs); // Asocia la lista de servidores a la configuración
        }

        currentElement = null;
    }

    // Método para validar la configuración
    private void validateConfiguration(Configuration config) throws SAXException {
        if (config.getHttpPort() <= 0) {
            throw new SAXException("El parámetro 'http' es obligatorio y no se ha definido correctamente.");
        }
        if (config.getWebServiceURL() == null || config.getWebServiceURL().isEmpty()) {
            throw new SAXException("El parámetro 'webservice' es obligatorio y no se ha definido.");
        }
        if (config.getNumClients() <= 0) {
            throw new SAXException("El parámetro 'numClients' es obligatorio y no se ha definido correctamente.");
        }
        if (config.getDbUser() == null || config.getDbUser().isEmpty()) {
            throw new SAXException("El parámetro 'user' es obligatorio y no se ha definido.");
        }
        if (config.getDbPassword() == null || config.getDbPassword().isEmpty()) {
            throw new SAXException("El parámetro 'password' es obligatorio y no se ha definido.");
        }
        if (config.getDbURL() == null || config.getDbURL().isEmpty()) {
            throw new SAXException("El parámetro 'url' es obligatorio y no se ha definido.");
        }
    }

    public Configuration getConfig() {
        return configuration;
    }
}
