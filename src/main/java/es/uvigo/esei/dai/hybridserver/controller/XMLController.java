package es.uvigo.esei.dai.hybridserver.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import es.uvigo.esei.dai.hybridserver.ServerConfiguration;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.XMLDAO;
import es.uvigo.esei.dai.hybridserver.model.XSDDAO;
import es.uvigo.esei.dai.hybridserver.model.XSLTDAO;
import es.uvigo.esei.dai.hybridserver.sax.SAXParsing;
import es.uvigo.esei.dai.hybridserver.sax.XSLUtils;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerServiceConnection;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerServiceUtils;
import es.uvigo.esei.dai.hybridserver.webservice.ServerConnection;
import es.uvigo.esei.dai.hybridserver.webservice.HybridServerService;
import jakarta.xml.ws.WebServiceException;

public class XMLController {
    private XMLDAO xmlDAO;
    private XSDDAO xsdDAO;
    private XSLTDAO xsltDAO;
	private List<ServerConfiguration> listServers;

    public XMLController(XMLDAO xmlDAO, XSDDAO xsdDAO, XSLTDAO xsltDAO, List<ServerConfiguration> listServers) {
        this.xmlDAO = xmlDAO;
        this.xsdDAO = xsdDAO;
        this.xsltDAO = xsltDAO;
        this.listServers = listServers;
    }
    
    public void handleXmlGet(String xmlID, HTTPResponse response, HTTPRequest request, int port) throws HTTPParseException {
        // Obtener parámetros de la solicitud
        String xsltID = request.getResourceParameters().get("xslt");
        String xsdID = null;

        String xsltContent = null;
        String xsdContent = null;
        String xmlContent = null;

        try {
            // Manejo del XSLT
            if (xsltID != null) {
                xsltContent = fetchXslt(xsltID);
                System.out.println(xsltContent);
                if (xsltContent == null) {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - XSLT not found for given UUID.");
                    return;
                }

                xsdID = xsltDAO.getXsd(xsltID);
                if (xsdID != null) {
                    xsdContent = fetchXsd(xsdID);
                    if (xsdContent == null) {
                        response.setStatus(HTTPResponseStatus.S404);
                        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                        response.setContent("404 Not Found - XSD not found for given UUID.");
                        return;
                    }
                }
            }

            // Manejo del XML
            if (xmlID != null) {
                xmlContent = fetchXml(xmlID);
                if (xmlContent == null) {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - XML not found for given UUID.");
                    return;
                }
            } else {
                // Cargar página de inicio si no se pasa un UUID
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent(generateXmlPageHome(port));
                return;
            }

            // Validar y transformar
            if (xsdContent != null && xmlContent != null) {
                try {
                    SAXParsing.parseAndValidateWithMemoryXSD(xmlContent, xsdContent);
                } catch (ParserConfigurationException | SAXException | IOException e) {
                    response.setStatus(HTTPResponseStatus.S400);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("400 Bad Request - XML is not valid against the provided XSD.");
                    return;
                }
            }

            if (xsltContent != null) {
                try {
                    String transformedContent = XSLUtils.transformWithMemoryXSLT(xmlContent, xsltContent);

                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent(transformedContent);
                } catch (Exception e) {
                    response.setStatus(HTTPResponseStatus.S400);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("400 Bad Request - Invalid XSLT.");
                    return;
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent(xmlContent);
            }
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    public void handleXmlPost(HTTPRequest request, HTTPResponse response) {
        try {
            String xmlContent = request.getResourceParameters().get("xml");

            if (xmlContent != null && !xmlContent.isEmpty()) {
                UUID uuid = UUID.randomUUID();
                xmlDAO.addDocument(uuid.toString(), xmlContent);
                StringBuilder content = new StringBuilder("<!DOCTYPE html>" +
                        "<html lang='es'>" + "<head><meta charset='utf-8'/>" +
                        "<title>Hybrid Server</title></head>" + "<body><h1>Hybrid Server</h1>" +
                        "<p>Nuevo documento añadido con UUID: <a href=\"xml?uuid=" + uuid + "\">" + uuid + "</a></p>" +
                        "</body></html>");

                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
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

    public void handleXmlDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xmlDAO.containsDocument(uuid)) {
                	xmlDAO.deleteDocument(uuid);
                    StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" 
                            + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>" + "<body>" 
                            + "<h1>Hybrid Server</h1>" + "<p>Documento con UUID: " + uuid + " eliminado exitosamente.</p>" 
                            + "</body>" + "</html>");

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
    
    // Generar la página principal del XML
    private String generateXmlPageHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "  <meta charset='utf-8'/>" +
            "  <title>Hybrid Server - XML</title>" +
            "</head>" +
            "<body>" +
            "<h1>Hybrid Server - XML</h1>" +
            "<h2>Local Server</h2>" +
            "<ul>");

        for (String uuidXml : xmlDAO.listDocuments()) {
            stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xml?uuid=" + uuidXml + "'>" + uuidXml + "</a></li>");
        }

        if (listServers != null) {
        	
        	List<ServerConnection> remoteConnections = HybridServerServiceUtils.getConnections(listServers);
    
            for (ServerConnection serverConnection : remoteConnections) {
                ServerConfiguration config = serverConnection.getConfiguration();
                HybridServerService connection = serverConnection.getConnection();

                stringBuilder.append("<h2>Servidor: " + config.getName() + "</h2><ul>");

                try {
                	List<String> uuidsXml = connection.getXmlUuids(); // Obtener los UUIDs de documentos remotos
	                
                	for (String uuidXml : uuidsXml) {
	                    stringBuilder.append("<li>UUID: <a href='" + config.getHttpAddress() + "xml?uuid=" + uuidXml + "'>" + uuidXml + "</a></li>");
	                }
	            } catch (Exception e) {
	                stringBuilder.append("<li>Error al obtener documentos de " + config.getName() + "</li>");
	                e.printStackTrace();
	            }
	            stringBuilder.append("</ul>");
	        }
        }

        stringBuilder.append("</ul></body></html>");
        return stringBuilder.toString();
    }

    // Buscar XML local y remoto
    private String fetchXml(String xmlID) {
        if (xmlDAO.containsDocument(xmlID)) {
            return xmlDAO.getDocument(xmlID);
        } else {
        	if (listServers != null) {
        		List<ServerConnection> connections = HybridServerServiceUtils.getConnections(listServers);
      
        		for (ServerConnection serverConnection : connections) {
                    HybridServerService connection = serverConnection.getConnection();
                   
                    try {
                        if (connection.getXmlUuids().contains(xmlID)) {
                            return connection.getXmlContent(xmlID);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener contenido del servidor: " + serverConnection.getConfiguration().getName());
                    }
                }
            }
        }
        
        return null;
    }

    // Buscar XSD local y remoto
    private String fetchXsd(String xsdID) {
        if (xsdDAO.containsSchema(xsdID)) {
            return xsdDAO.getSchema(xsdID);
        } else {
        	if (listServers != null) {
        		List<ServerConnection> connections = HybridServerServiceUtils.getConnections(listServers);
      
        		for (ServerConnection serverConnection : connections) {
                    HybridServerService connection = serverConnection.getConnection();
                   
                    try {
                        if (connection.getXsdUuids().contains(xsdID)) {
                            return connection.getXsdContent(xsdID);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener contenido del servidor: " + serverConnection.getConfiguration().getName());
                    }
                }
            }
        }
        
        return null;
    }

    // Buscar XSLT local y remoto
    private String fetchXslt(String xsltID) {
        if (xsltDAO.containsStylesheet(xsltID)) {
            return xsltDAO.getStylesheet(xsltID);
        } else {
        	if (listServers != null) {
        		List<ServerConnection> connections = HybridServerServiceUtils.getConnections(listServers);
      
        		for (ServerConnection serverConnection : connections) {
                    HybridServerService connection = serverConnection.getConnection();
                   
                    try {
                        if (connection.getXsltUuids().contains(xsltID)) {
                            return connection.getXsltContent(xsltID);
                        }
                    } catch (Exception e) {
                        System.err.println("Error al obtener contenido del servidor: " + serverConnection.getConfiguration().getName());
                    }
                }
            }
        }
        
        return null;
    }

}
