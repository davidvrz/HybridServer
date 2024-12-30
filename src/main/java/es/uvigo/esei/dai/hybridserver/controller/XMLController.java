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
    private XMLDAO xmlDAO;

    public XMLController(XMLDAO dao) {
        this.xmlDAO = dao;
    }
    /*
    public void handleXmlGet(String uuid, HTTPResponse response, int port, HTTPRequest request) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xmlDAO.containsDocument(uuid)) {
                    String xmlContent = xmlDAO.getDocument(uuid);

                    // Revisamos si hay un parámetro 'xslt' en la solicitud
                    String xsltId = request.getResourceParameters().get("xslt");
                    
                    if (xsltId != null && !xsltId.isEmpty()) {
                        // Intentamos obtener la plantilla XSLT y el esquema XSD asociados
                        String xsltTemplate = xmlDAO.getXslTemplate(xsltId);
                        String xsdSchema = xmlDAO.getXsd(xsltId);

                        if (xsltTemplate != null && xsdSchema != null) {
                            // Validamos el XML con el esquema XSD
                            boolean isValid = validateXmlWithXsd(xmlContent, xsdSchema);
                            
                            if (isValid) {
                                // Realizamos la transformación XSLT sobre el XML
                                String transformedXml = transformXmlWithXslt(xmlContent, xsltTemplate);
                                
                                // Respondemos con el XML transformado
                                response.setStatus(HTTPResponseStatus.S200);
                                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                                response.setContent(transformedXml);
                            } else {
                                // Si la validación falla, respondemos con un error 400
                                response.setStatus(HTTPResponseStatus.S400);
                                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                                response.setContent("400 Bad Request - XML failed validation against the provided XSD schema.");
                            }
                        } else {
                            // Si no se encuentra la plantilla XSLT o el esquema XSD
                            response.setStatus(HTTPResponseStatus.S404);
                            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                            response.setContent("404 Not Found - XSLT template or XSD schema not found for given XSLT ID.");
                        }
                    } else {
                        // Si no se proporciona parámetro 'xslt', simplemente devolvemos el XML
                        response.setStatus(HTTPResponseStatus.S200);
                        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                        response.setContent(xmlContent);
                    }
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
*/

    public void handleXmlGet(String uuid, HTTPResponse response, int port) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
                if (xmlDAO.containsDocument(uuid)) {
                    String xmlContent = xmlDAO.getDocument(uuid);
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
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
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
