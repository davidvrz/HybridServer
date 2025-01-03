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
import es.uvigo.esei.dai.hybridserver.webservice.WebServiceConnection;
import es.uvigo.esei.dai.hybridserver.webservice.WebServiceInterface;
import jakarta.xml.ws.WebServiceException;

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
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" 
        		+ "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" 
        		+ "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<ul>");

        for (String documentUUID : htmlDAO.listDocuments()) {
        	stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/html?uuid=" + documentUUID + "'>" + documentUUID + "</a></li>");
        }
        
        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Añadir nueva página</h2>" + "<form action='/html' method='POST'>" + "<textarea name='html'></textarea>" + "<button type='submit'>Submit</button>" + "</form>" + "</body></html>");
        stringBuilder.append("</body>" + "</html>");
        
        return stringBuilder.toString();
    }
    
    public String fetchContentFromOtherServers(String uuid) {
		if (listServers != null) {
            for (ServerConfiguration serverConfig : listServers) {
                try {
                    WebServiceConnection wsc = new WebServiceConnection(
                        serverConfig.getName(),
                        serverConfig.getWsdl(),
                        serverConfig.getNamespace(),
                        serverConfig.getService(),
                        serverConfig.getHttpAddress()
                    );
                    WebServiceInterface ws = wsc.setConnection();
                    if (ws.getHtmlUuids().contains(uuid)) {
                        return ws.getHtmlContent(uuid);
                    }
                } catch (WebServiceException e) {
                    System.out.println("Failed to connect to server: " + serverConfig.getName());
                }
            }
        }
        return null;
    }
}
