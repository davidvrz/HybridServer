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
import es.uvigo.esei.dai.hybridserver.model.HTMLDAO;
import es.uvigo.esei.dai.hybridserver.webservice.DocumentServiceUtils;
import es.uvigo.esei.dai.hybridserver.webservice.ServerConnection;
import es.uvigo.esei.dai.hybridserver.webservice.DocumentService;

public class HTMLController {
    private HTMLDAO htmlDAO;
	private List<ServerConfiguration> listServers;
    
    public HTMLController(HTMLDAO dao, List<ServerConfiguration> listServers) {
        this.htmlDAO = dao;
        this.listServers= listServers;
    }

    public void handleHtmlGet(String uuid, HTTPResponse response, int port) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (htmlDAO.containsDocument(uuid)) {
                    String documentContent = htmlDAO.getDocument(uuid);
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent(documentContent);
                } else {
                    String remoteContent = fetchContentFromOtherServers(uuid);
                    if (remoteContent != null) {
                        response.setStatus(HTTPResponseStatus.S200);
                        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                        response.setContent(remoteContent);
                    } else {
                        response.setStatus(HTTPResponseStatus.S404);
                        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                        response.setContent("404 Not Found - Document not found for given UUID");
                    }
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent(generateHtmlPageHome(port));
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

    public void handleHtmlPost(HTTPRequest request, HTTPResponse response) {
        try {
            String htmlContent = request.getResourceParameters().get("html");
            
            if (htmlContent != null && !htmlContent.isEmpty()) {
            	UUID uuid = UUID.randomUUID();
            	htmlDAO.addDocument(uuid.toString(), htmlContent);
                StringBuilder content = new StringBuilder("<!DOCTYPE html>" +
                        "<html lang='es'>" + "<head><meta charset='utf-8'/>" +
                        "<title>Hybrid Server</title></head>" + "<body><h1>Hybrid Server</h1>" +
                        "<p>Nueva página añadida con UUID: <a href=\"html?uuid=" + uuid + "\">" + uuid + "</a></p>" +
                        "</body></html>");
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent(content.toString());
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("400 Bad Request - HTML content is missing");
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

    
    public void handleHtmlDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
            	if (htmlDAO.containsDocument(uuid)){
            		htmlDAO.deleteDocument(uuid);
                    StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" 
                    + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>" + "<body>" 
                    + "<h1>Hybrid Server</h1>" + "<p>Página con UUID: " + uuid + " eliminada exitosamente.</p>" 
                    + "</body>" + "</html>");
                    
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent(content.toString());
            	}  else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - Document not found for given UUID");
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

    private String generateHtmlPageHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "  <meta charset='utf-8'/>" +
            "  <title>Hybrid Server</title>" +
            "</head>" +
            "<body>" +
            "<h1>Hybrid Server</h1>" +
            "<h2>Local Server</h2>" +
            "<ul>");

        // Mostrar los documentos locales
        for (String documentUUID : htmlDAO.listDocuments()) {
            stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/html?uuid=" + documentUUID + "'>" + documentUUID + "</a></li>");
        }

        // Ahora buscamos los documentos remotos, conectamos a los servidores remotos y obtenemos los UUIDs
        if (listServers != null) {
            List<ServerConnection> remoteConnections = DocumentServiceUtils.getConnections(listServers);

            for (ServerConnection serverConnection : remoteConnections) {
                ServerConfiguration config = serverConnection.getConfiguration();
                DocumentService connection = serverConnection.getConnection();

                stringBuilder.append("<h2>Servidor: " + config.getName() + "</h2><ul>");

                try {
                    List<String> uuidsHtml = connection.getHtmlUuids(); // Obtener los UUIDs de documentos remotos

                    for (String uuidHtml : uuidsHtml) {
                        stringBuilder.append("<li>UUID: <a href='" + config.getHttpAddress() + "/html?uuid=" + uuidHtml + "'>" + uuidHtml + "</a></li>");
                    }
                } catch (Exception e) {
                    stringBuilder.append("<li>Error al obtener documentos de " + config.getName() + "</li>");
                }

                stringBuilder.append("</ul>");
            }
        }

        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Añadir nueva página</h2>" +
            "<form action='/html' method='POST'>" +
            "<textarea name='html'></textarea>" +
            "<button type='submit'>Submit</button>" +
            "</form>" +
            "</body>" +
            "</html>");

        return stringBuilder.toString();
    }

    
    private String fetchContentFromOtherServers(String uuid) {
        if (listServers != null) {
            // Llamamos a los servidores remotos para obtener el contenido del documento
            List<ServerConnection> connections = DocumentServiceUtils.getConnections(listServers);

            for (ServerConnection serverConnection : connections) {
                DocumentService connection = serverConnection.getConnection();

                try {
                    if (connection.getHtmlUuids().contains(uuid)) {
                        return connection.getHtmlContent(uuid);
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener contenido del servidor: " + serverConnection.getConfiguration().getName());
                }
            }
        }

        return null;
    }

}
