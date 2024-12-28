package es.uvigo.esei.dai.hybridserver.controller;

import java.util.UUID;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.HTMLDAO;

public class HTMLController {
    private HTMLDAO HTMLDAO;
    
    public HTMLController(HTMLDAO dao) {
        this.HTMLDAO = dao;
    }

    public void handleGetRequest(HTTPRequest request, HTTPResponse response, int port) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "html":
                handleHtmlGet(uuid, response, port);
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
    	if (request.getResourceParameters().containsKey("html")) {
    		 handleHtmlPost(request, response);
    	} else {
    		 response.setStatus(HTTPResponseStatus.S400);
             response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
    	     response.setContent("400 Bad Request - 'html' parameter is missing");
    	}
    }
    
    public void handleDeleteRequest(HTTPRequest request,HTTPResponse response){
    	System.out.println("Handling DELETE request");
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");
        
        switch (resource) {
            case "html":
                handleHtmlDelete(uuid, response);
                break;
            default:
                handleNotFound(response);
                break;
        }        
    }

    private void handleHtmlGet(String uuid, HTTPResponse response, int port) {
        try {
        	if (uuid != null && !uuid.isEmpty()) {
            	if (HTMLDAO.containsDocument(uuid)){
	                String documentContent = HTMLDAO.getDocument(uuid);	              
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent(documentContent);
	            } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    response.setContent("404 Not Found - Document not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent(generateHtmlDocumentHome(port));
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

    private void handleHtmlPost(HTTPRequest request, HTTPResponse response) {
        try {
            String htmlContent = request.getResourceParameters().get("html");
            
            if (htmlContent != null && !htmlContent.isEmpty()) {
            	UUID uuid = UUID.randomUUID();
                HTMLDAO.addDocument(uuid.toString(), htmlContent);
                StringBuilder content = new StringBuilder("<!DOCTYPE html>" +
                        "<html lang='es'>" +
                        "<head><meta charset='utf-8'/>" +
                        "<title>Hybrid Server</title></head>" +
                        "<body><h1>Hybrid Server</h1>" +
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

    
    private void handleHtmlDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null && !uuid.isEmpty()) {
            	if (HTMLDAO.containsDocument(uuid)){
            		HTMLDAO.deleteDocument(uuid);
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


    private void handleWelcomePage(HTTPResponse response, int port) {
    	StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>"
				+ "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>"
				+ "<body>" + "<h1>Hybrid Server</h1>"
				+ "<p>Autores: David Álvarez Iglesias, Antonio Caride Pernas.</p>" + "<a href='http://localhost:" + port + "/html'>Lista de Páginas HTML</a>"+
				"</body>" + "</html>");

		response.setStatus(HTTPResponseStatus.S200);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
		response.setContent(stringBuilder.toString());
    }
    
    private String generateHtmlDocumentHome(int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" 
        		+ "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" 
        		+ "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<ul>");

        for (String documentUUID : HTMLDAO.listDocuments()) {
        	stringBuilder.append("<li>UUID: <a href='http://localhost:" + port + "/html?uuid=" + documentUUID + "'>" + documentUUID + "</a></li>");
        }
        
        stringBuilder.append("</ul>");

        stringBuilder.append("<h2>Añadir nueva página</h2>" + "<form action='/html' method='POST'>" + "<textarea name='html'></textarea>" + "<button type='submit'>Submit</button>" + "</form>" + "</body></html>");
        
        stringBuilder.append("</body>" + "</html>");
        
        return stringBuilder.toString();
    }

    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S400);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
        response.setContent("400 Bad Request");
    }
}
