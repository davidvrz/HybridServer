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
    private XSDDAO XSDDAO;

    public XSDController(XSDDAO dao) {
        this.XSDDAO = dao;
    }

    public void handleGetRequest(HTTPRequest request, HTTPResponse response, int port) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "xsd":
                handleXsdGet(uuid, response, port);
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
        if (request.getResourceParameters().containsKey("xsd")) {
            handleXsdPost(request, response);
        } else {
            response.setStatus(HTTPResponseStatus.S400);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("400 Bad Request - 'xsd' parameter is missing");
        }
    }

    public void handleDeleteRequest(HTTPRequest request, HTTPResponse response) {
        System.out.println("Handling DELETE request");
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "xsd":
                handleXsdDelete(uuid, response);
                break;
            default:
                handleNotFound(response);
                break;
        }
    }

    private void handleXsdGet(String uuid, HTTPResponse response, int port) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (XSDDAO.containsSchema(uuid)) {
                    String xsdContent = XSDDAO.getSchema(uuid);
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
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
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

    private void handleXsdPost(HTTPRequest request, HTTPResponse response) {
        try {
            String xsdContent = request.getResourceParameters().get("xsd");

            if (xsdContent != null && !xsdContent.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                XSDDAO.addSchema(uuid.toString(), xsdContent);
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

    private void handleXsdDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (XSDDAO.containsSchema(uuid)) {
                    XSDDAO.deleteSchema(uuid);
                    StringBuilder content = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                            "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
                            "<xsd:element name='deleteXsd' type='xsd:string'/>" +
                            "<p>XSD Schema con UUID: " + uuid + " eliminada exitosamente.</p>" +
                            "</xsd:schema>");

                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
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

    private void handleWelcomePage(HTTPResponse response, int port) {
        StringBuilder stringBuilder = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
                "<xsd:element name='welcome' type='xsd:string'/>" +
                "<p>Hybrid Server</p>" +
                "<a href='http://localhost:" + port + "/xsd'>Lista de XSD</a>" +
                "</xsd:schema>");

        response.setStatus(HTTPResponseStatus.S200);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
        response.setContent(stringBuilder.toString());
    }

    private String generateXsdPageHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>" +
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
                "<xsd:element name='home' type='xsd:string'/>" +
                "<h1>Lista de XSDs</h1><ul>");

        for (String schemaUUID : XSDDAO.listSchemas()) {
            stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xsd?uuid=" + schemaUUID + "'>" + schemaUUID + "</a></li>");
        }

        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Añadir nuevo XSD</h2>" +
                "<form action='/xsd' method='POST'>" +
                "<textarea name='xsd'></textarea>" +
                "<button type='submit'>Submit</button>" +
                "</form>" +
                "</xsd:schema>");
        
        return stringBuilder.toString();
    }

    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S400);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
        response.setContent("400 Bad Request");
    }
}
