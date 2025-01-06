package es.uvigo.esei.dai.hybridserver.controller;

import java.util.List;
import java.util.UUID;

import es.uvigo.esei.dai.hybridserver.ServerConfiguration;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.XSDDAO;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerServiceConnection;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerServiceUtils;
import es.uvigo.esei.dai.hybridserver.webservice.ServerConnection;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerService;
import jakarta.xml.ws.WebServiceException;

public class XSDController {
    private XSDDAO xsdDAO;
	private List<ServerConfiguration> listServers;

    public XSDController(XSDDAO dao, List<ServerConfiguration> listServers) {
        this.xsdDAO = dao;
        this.listServers= listServers;
    }

    public void handleXsdGet(String uuid, HTTPResponse response, int port) {
        try {
            // Si se proporciona un UUID específico
            if (uuid != null && !uuid.isEmpty()) {
                if (xsdDAO.containsSchema(uuid)) {
                    // Esquema XSD encontrado localmente
                    String schemaContent = xsdDAO.getSchema(uuid);
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(schemaContent);
                } else {
                    // Buscar en servidores remotos
                    String remoteContent = fetchContentFromOtherServers(uuid);
                    if (remoteContent != null) {
                        response.setStatus(HTTPResponseStatus.S200);
                        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                        response.setContent(remoteContent);
                    } else {
                        // Esquema XSD no encontrado
                        response.setStatus(HTTPResponseStatus.S404);
                        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                        response.setContent("404 Not Found - XSD Schema not found for given UUID.");
                    }
                }
            } else {
                // Generar la página principal con los esquemas XSD disponibles
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent(generateXsdPageHome(port));
            }
        } catch (JDBCException e) {
            // Manejo de errores específicos de la base de datos
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            // Manejo de errores generales
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    public void handleXsdPost(HTTPRequest request, HTTPResponse response) {
        try {
            String xsdContent = request.getResourceParameters().get("xsd");

            if (xsdContent != null && !xsdContent.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                xsdDAO.addSchema(uuid.toString(), xsdContent);
                StringBuilder content = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
                        "<xsd:element name='newXsd' type='xsd:string'/>" +
                        "<p>Nueva XSD añadida con UUID: <a href=\"xsd?uuid=" + uuid + "\">" + uuid + "</a></p>" +
                        "</xsd:schema>");

                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent(content.toString());
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("400 Bad Request - XSD content is missing");
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    public void handleXsdDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xsdDAO.containsSchema(uuid)) {
                	xsdDAO.deleteSchema(uuid);
                    StringBuilder content = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                            "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
                            "<xsd:element name='deleteXsd' type='xsd:string'/>" +
                            "<p>XSD Schema con UUID: " + uuid + " eliminada exitosamente.</p>" +
                            "</xsd:schema>");

                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent(content.toString());
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - XSD Schema not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("400 Bad Request - UUID is missing");
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    private String generateXsdPageHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "  <meta charset='utf-8'/>" +
            "  <title>Hybrid Server - XSD</title>" +
            "</head>" +
            "<body>" +
            "<h1>Hybrid Server - XSD</h1>" +
            "<h2>Local Server</h2>" +
            "<ul>");

        // Listar los esquemas XSD locales
        for (String uuidXsd : xsdDAO.listSchemas()) {
            stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xsd?uuid=" + uuidXsd + "'>" + uuidXsd + "</a></li>");
        }

        // Buscar documentos en servidores remotos
        if (listServers != null) {
            List<ServerConnection> remoteConnections = HybridServerServiceUtils.getConnections(listServers);

            for (ServerConnection serverConnection : remoteConnections) {
                ServerConfiguration config = serverConnection.getConfiguration();
                HybridServerService connection = serverConnection.getConnection();
                stringBuilder.append("<h2>Servidor: " + config.getName() + "</h2><ul>");

                try {
                    List<String> uuidsXsd = connection.getXsdUuids(); // Obtener UUIDs remotos
                    for (String uuidXsd : uuidsXsd) {
                        stringBuilder.append("<li>UUID: <a href='" + config.getHttpAddress() + "xsd?uuid=" + uuidXsd + "'>" + uuidXsd + "</a></li>");
                    }
                } catch (Exception e) {
                    stringBuilder.append("<li>Error al obtener esquemas de " + config.getName() + "</li>");
                    e.printStackTrace();
                }

                stringBuilder.append("</ul>");
            }
        }

        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Añadir nuevo esquema</h2>" +
            "<form action='/xsd' method='POST'>" +
            "<textarea name='xsd'></textarea>" +
            "<button type='submit'>Submit</button>" +
            "</form>" +
            "</body>" +
            "</html>");

        return stringBuilder.toString();
    }

    private String fetchContentFromOtherServers(String uuid) {
        if (listServers != null) {
            // Buscar en servidores remotos
            List<ServerConnection> connections = HybridServerServiceUtils.getConnections(listServers);

            for (ServerConnection serverConnection : connections) {
                HybridServerService connection = serverConnection.getConnection();

                try {
                    if (connection.getXsdUuids().contains(uuid)) {
                        return connection.getXsdContent(uuid);
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener contenido del servidor: " + serverConnection.getConfiguration().getName());
                }
            }
        }

        return null;
    }
}
