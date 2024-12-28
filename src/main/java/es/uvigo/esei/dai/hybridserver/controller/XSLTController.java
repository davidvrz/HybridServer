package es.uvigo.esei.dai.hybridserver.controller;

import java.util.UUID;

import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.XSLTDAO;

public class XSLTController {
    private XSLTDAO xsltDAO;

    public XSLTController(XSLTDAO dao) {
        this.xsltDAO = dao;
    }

    public void handleGetRequest(HTTPRequest request, HTTPResponse response, int port) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "xslt":
                handleXsltGet(uuid, response, port);
                break;
            /*case "":
                handleWelcomePage(response, port);
                break;*/
            default:
                handleNotFound(response);
                break;
        }
    }

    public void handlePostRequest(HTTPRequest request, HTTPResponse response) {
        if (request.getResourceParameters().containsKey("xslt")) {
            handleXsltPost(request, response);
        } else {
            response.setStatus(HTTPResponseStatus.S400);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("400 Bad Request - 'xslt' parameter is missing");
        }
    }

    public void handleDeleteRequest(HTTPRequest request, HTTPResponse response) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "xslt":
                handleXsltDelete(uuid, response);
                break;
            default:
                handleNotFound(response);
                break;
        }
    }

    private void handleXsltGet(String uuid, HTTPResponse response, int port) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xsltDAO.containsStylesheet(uuid)) {
                    String xsltContent = xsltDAO.getStylesheet(uuid);
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(xsltContent);
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent("404 Not Found - Stylesheet not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent(generateXsltHome(port));
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    private void handleXsltPost(HTTPRequest request, HTTPResponse response) {
        try {
            String xsltContent = request.getResourceParameters().get("xslt");
            String xsdUUID = request.getResourceParameters().get("xsd");

            if (xsltContent != null && !xsltContent.isEmpty() && xsdUUID != null && !xsdUUID.isEmpty()) {
                String xsd = xsltDAO.getXsd(xsdUUID);
                if (xsd == null) {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent("404 Not Found - XSD not found for given UUID");
                } else {
                    UUID uuid = UUID.randomUUID();
                    xsltDAO.addStylesheet(uuid.toString(), xsltContent, xsdUUID);
                    StringBuilder content = new StringBuilder("<!DOCTYPE html>" +
                            "<html lang='es'>" +
                            "<head><meta charset='utf-8'/>" +
                            "<title>Hybrid Server</title></head>" +
                            "<body><h1>Hybrid Server</h1>" +
                            "<p>New XSLT stylesheet added with UUID: <a href=\"xslt?uuid=" + uuid + "\">" + uuid + "</a></p>" +
                            "</body></html>");
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(content.toString());
                }
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent("400 Bad Request - Missing XSLT content or XSD UUID");
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    private void handleXsltDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xsltDAO.containsStylesheet(uuid)) {
                    xsltDAO.deleteStylesheet(uuid);
                    StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" +
                            "<meta charset='utf-8'/>" + "<title>Hybrid Server</title>" + "</head>" + "<body>" +
                            "<h1>Hybrid Server</h1>" + "<p>Stylesheet with UUID: " + uuid + " deleted successfully.</p>" +
                            "</body>" + "</html>");

                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(content.toString());
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent("404 Not Found - Stylesheet not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent("400 Bad Request - UUID is missing");
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    private String generateXsltHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" +
                "<head>" + "<meta charset='utf-8'/>" + "<title>Hybrid Server</title>" + "</head>" +
                "<body>" + "<h1>Hybrid Server</h1>" + "<ul>");

        for (String stylesheetUUID : xsltDAO.listStylesheets()) {
            stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xslt?uuid=" + stylesheetUUID + "'>" + stylesheetUUID + "</a></li>");
        }

        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Add new stylesheet</h2>" + "<form action='/xslt' method='POST'>" +
                "<textarea name='xslt'></textarea>" + "<input type='text' name='xsd' placeholder='XSD UUID'/>" +
                "<button type='submit'>Submit</button>" + "</form>" + "</body></html>");
        stringBuilder.append("</body>" + "</html>");

        return stringBuilder.toString();
    }

    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S400);
        response.setContent("400 Bad Request");
    }
}
