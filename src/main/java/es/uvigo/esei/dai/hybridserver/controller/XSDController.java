package es.uvigo.esei.dai.hybridserver.controller;

import java.util.UUID;

import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.XSDDAO;

public class XSDController {
    private XSDDAO xsdDAO;

    public XSDController(XSDDAO dao) {
        this.xsdDAO = dao;
    }

    public void handleXsdGet(String uuid, HTTPResponse response, int port) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xsdDAO.containsSchema(uuid)) {
                    String xsdContent = xsdDAO.getSchema(uuid);
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(xsdContent);
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - XSD Schema not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent(generateXsdPageHome(port));
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
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" 
        		+ "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" 
        		+ "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<ul>");

        for (String documentUUID : xsdDAO.listSchemas()) {
        	stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xsd?uuid=" + documentUUID + "'>" + documentUUID + "</a></li>");
        }
        
        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Añadir nueva página</h2>" + "<form action='/xsd' method='POST'>" + "<textarea name='xsd'></textarea>" + "<button type='submit'>Submit</button>" + "</form>" + "</body></html>");
        stringBuilder.append("</body>" + "</html>");
        
        return stringBuilder.toString();
    }
}
