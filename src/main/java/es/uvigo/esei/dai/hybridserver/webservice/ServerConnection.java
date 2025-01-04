package es.uvigo.esei.dai.hybridserver.webservice;

import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

public class ServerConnection {
    private ServerConfiguration configuration;
    private DocumentService connection;

    public ServerConnection(ServerConfiguration configuration, DocumentService connection) {
        this.configuration = configuration;
        this.connection = connection;
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    public DocumentService getConnection() {
        return connection;
    }
}

