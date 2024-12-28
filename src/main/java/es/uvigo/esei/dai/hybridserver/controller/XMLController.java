package es.uvigo.esei.dai.hybridserver.controller;

import java.util.UUID;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.XMLDAO;

public class XMLController {
    private XMLDAO XMLDAO;

    public XMLController(XMLDAO dao) {
        this.XMLDAO = dao;
    }

    public void handleGetRequest(HTTPRequest request, HTTPResponse response, int port) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "xml":
                handleXmlGet(uuid, response, port);
                break;
            case "":
                handleWelcomePage(response, port);
                break;
            default:
                handleNotFound(response);
                break;
        }
    }

    public void handlePostRequest(HTTPRequest request, HTTPResponse response) {
        if (request.getResourceParameters().containsKey("xml")) {
            handleXmlPost(request, response);
        } else {
            response.setStatus(HTTPResponseStatus.S400);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("400 Bad Request - 'xml' parameter is missing");
        }
    }

    public void handleDeleteRequest(HTTPRequest request, HTTPResponse response) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "xml":
                handleXmlDelete(uuid, response);
                break;
            default:
                handleNotFound(response);
                break;
        }
    }

    private void handleXmlGet(String uuid, HTTPResponse response, int port) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (XMLDAO.containsDocument(uuid)) {
                    String xmlContent = XMLDAO.getDocument(uuid);
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(xmlContent);
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - XML document not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent(generateXmlPageHome(port));
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

    private void handleXmlPost(HTTPRequest request, HTTPResponse response) {
        try {
            String xmlContent = request.getResourceParameters().get("xml");

            if (xmlContent != null && !xmlContent.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                XMLDAO.addDocument(uuid.toString(), xmlContent);
                StringBuilder content = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                        "<response>" +
                        "<message>New XML document added with UUID: <a href=\"xml?uuid=" + uuid + "\">" + uuid + "</a></message>" +
                        "</response>");

                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent(content.toString());
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("400 Bad Request - XML content is missing");
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

    private void handleXmlDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (XMLDAO.containsDocument(uuid)) {
                    XMLDAO.deleteDocument(uuid);
                    StringBuilder content = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                            "<response>" +
                            "<message>XML document with UUID: " + uuid + " successfully deleted.</message>" +
                            "</response>");

                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                    response.setContent(content.toString());
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - XML document not found for given UUID");
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

    private void handleWelcomePage(HTTPResponse response, int port) {
        StringBuilder stringBuilder = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                "<response>" +
                "<message>Welcome to the Hybrid Server</message>" +
                "<a href='http://localhost:" + port + "/xml'>List of XML Documents</a>" +
                "</response>");

        response.setStatus(HTTPResponseStatus.S200);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
        response.setContent(stringBuilder.toString());
    }

    private String generateXmlPageHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                "<response>" +
                "<h1>List of XML Documents</h1><ul>");

        for (String documentUUID : XMLDAO.listDocuments()) {
            stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xml?uuid=" + documentUUID + "'>" + documentUUID + "</a></li>");
        }

        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Add New XML Document</h2>" +
                "<form action='/xml' method='POST'>" +
                "<textarea name='xml'></textarea>" +
                "<button type='submit'>Submit</button>" +
                "</form>" +
                "</response>");
        
        return stringBuilder.toString();
    }

    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S400);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
        response.setContent("400 Bad Request");
    }
}
