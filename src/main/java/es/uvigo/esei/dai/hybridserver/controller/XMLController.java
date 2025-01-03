package es.uvigo.esei.dai.hybridserver.controller;

import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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

public class XMLController {
    private XMLDAO xmlDAO;
    private XSDDAO xsdDAO;
    private XSLTDAO xsltDAO;

    public XMLController(XMLDAO xmlDAO, XSDDAO xsdDAO, XSLTDAO xsltDAO) {
        this.xmlDAO = xmlDAO;
        this.xsdDAO = xsdDAO;
        this.xsltDAO = xsltDAO;
    }
    
    public void handleXmlGet(String xmlID, HTTPResponse response, HTTPRequest request, int port) throws HTTPParseException {
        // Obtener parámetros de la solicitud
        String xsltID = request.getResourceParameters().get("xslt");
        String xsdID = null;
        
        String xsltContent = null;
        String xsdContent = null;
        String xmlContent = null;

        // Manejo del parámetro xslt
        if (xsltID != null) {
            if (xsltDAO.containsStylesheet(xsltID)) {
            	xsdID = xsltDAO.getXsd(xsltID);
            	xsltContent = xsltDAO.getStylesheet(xsltID);
            } else {
                response.setStatus(HTTPResponseStatus.S404);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("404 Not Found - Document not found for given xslt uuid");
                return;
            }

            if (xsdID != null && xsdDAO.containsSchema(xsdID)) {
            		xsdContent = xsdDAO.getSchema(xsdID);            	
            } else {
                response.setStatus(HTTPResponseStatus.S404);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("404 Not Found - Document not found for given xsd uuid");
                return;
            }
        }

        // Manejo del parámetro uuid (XML)
        if (xmlID != null) {
            if (xmlDAO.containsDocument(xmlID)) {
            	xmlContent = xmlDAO.getDocument(xmlID);
            } else {
                // Si no se encuentra el XML, devolver 404
                response.setStatus(HTTPResponseStatus.S404);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("404 Not Found - Document not found for given xml uuid");
                return;
            }
        } else {
        	 response.setStatus(HTTPResponseStatus.S200);
             response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
             response.setContent(generateXmlPageHome(port));
             return;
        }

        // Validar y transformar (si corresponde)
        if (xsdContent != null && xmlContent != null) {
        	try {
    	        SAXParsing.parseAndValidateWithMemoryXSD(xmlContent, xsdContent);
    	    } catch (ParserConfigurationException | SAXException | IOException e) {
    	        // Si el XML no es válido contra el XSD, devolver error 400
    	        response.setStatus(HTTPResponseStatus.S400);
    	        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("400 Bad Request - XML is not valid against the provided XSD.");
    	        return;
    	    }

            // Si hay un XSLT asociado, transformar
            if (xsltContent != null) {
                try {
                    String transformedContent = XSLUtils.transformWithMemoryXSLT(xmlContent, xsltContent);
    
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent(transformedContent);
                } catch (Exception e) {
                    // Si hay un error durante la transformación, devolver error 500
                    response.setStatus(HTTPResponseStatus.S500);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("500 Internal Server Error - Error occurred while transforming XML with XSLT.");
                    return;
                }
                
            } else {
                // Si el XML es válido contra el XSD
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                response.setContent(xmlContent);
            }
 
        } else {
        	 response.setStatus(HTTPResponseStatus.S200);
             response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
             response.setContent(xmlContent);
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

    private String generateXmlPageHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" 
        		+ "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" 
        		+ "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<ul>");

        for (String documentUUID : xmlDAO.listDocuments()) {
        	stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/xml?uuid=" + documentUUID + "'>" + documentUUID + "</a></li>");
        }
        
        stringBuilder.append("</ul>");
        stringBuilder.append("<h2>Añadir nueva página</h2>" + "<form action='/xml' method='POST'>" + "<textarea name='xml'></textarea>" + "<button type='submit'>Submit</button>" + "</form>" + "</body></html>");
        stringBuilder.append("</body>" + "</html>");
        
        return stringBuilder.toString();
    }

}
