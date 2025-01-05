package es.uvigo.esei.dai.hybridserver.webservice;

import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

public class ServerConnection {
    private ServerConfiguration configuration;
    private HybridServerService connection;

    public ServerConnection(ServerConfiguration configuration, HybridServerService connection) {
        this.configuration = configuration;
        this.connection = connection;
    }

    public ServerConfiguration getConfiguration() {
        return this.configuration;
    }

    public HybridServerService getConnection() {
        return this.connection;
    }
}

