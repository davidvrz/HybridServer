package es.uvigo.esei.dai.hybridserver.controller;

import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;
import es.uvigo.esei.dai.hybridserver.model.PagesDBDAO;



public class ResourceController {
    private PagesDBDAO pagesDAO;

    public ResourceController() {
        this.pagesDAO = new PagesDBDAO();
    }

    public void handleGetRequest(HTTPRequest request, HTTPResponse response) {
        String resource = request.getResourceName();
        String uuid = request.getResourceParameters().get("uuid");

        switch (resource) {
            case "html":
                handleHtmlGet(uuid, response);
                break;
            case "":
                handleWelcomePage(response);
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
    		handleNotFound(response);
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

    private void handleHtmlGet(String uuid, HTTPResponse response) {
        try {
            if (uuid != null) { 
                String pageContent = pagesDAO.getPageByUUID(uuid);
                if (pageContent != null) { 
                    response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime() + "; charset=UTF-8");
                    response.setContent(pageContent);
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.setContent("404 Not Found - Page not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime() + "; charset=UTF-8");
                response.setContent(generateHtmlPageHome());
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    private void handleHtmlPost(HTTPRequest request, HTTPResponse response) {
        try {
            String htmlContent = request.getResourceParameters().get("html");
            
            if (htmlContent != null && !htmlContent.isEmpty()) {
                String newUUID = pagesDAO.savePage(htmlContent);
                StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<p>Nueva página añadida con UUID: <a href='http://localhost:8888/html?uuid=" + newUUID + "'>" + newUUID + "</a>" + "</p>" + "</body>" + "</html>");
                response.setStatus(HTTPResponseStatus.S200);
                response.setContent(content.toString());
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.setContent("400 Bad Request - HTML content is missing");
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }

    
    private void handleHtmlDelete(String uuid, HTTPResponse response) {
        try {
            if (uuid != null) {
                pagesDAO.deletePage(uuid);
                StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<p>Página con UUID: " + uuid + " eliminada exitosamente.</p>" + "</body>" + "</html>");
                response.setStatus(HTTPResponseStatus.S200);
                response.setContent(content.toString());
            } else {
                response.setStatus(HTTPResponseStatus.S400);
                response.setContent("400 Bad Request - UUID is missing");
            }
        } catch (JDBCException e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.setContent("500 Internal Server Error - " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HTTPResponseStatus.S500);
            response.setContent("500 Internal Server Error - An unexpected error occurred.");
        }
    }


    private void handleWelcomePage(HTTPResponse response) {
    	StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<p>Autores:david Álvarez Iglesias, Antonio Caride Pernas.</p>" + "</body>" + "</html>");
        response.setStatus(HTTPResponseStatus.S200);
        response.setContent(content.toString());
    }
    
    private String generateHtmlPageHome() {
        StringBuilder content = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>" + "  <meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>" + "<body>" + "<h1>Hybrid Server</h1>" + "<ul>");

        for (String pageUUID : pagesDAO.listPages()) {
            content.append("<li>UUID: <a href='http://localhost:8888/html?uuid=" + pageUUID + "'>" + pageUUID + "</a></li>");
        }
        
        content.append("</ul>");

        content.append("<h2>Añadir nueva página</h2>" + "<form action='/html' method='POST'>" + "<textarea name='html'></textarea>" + "<button type='submit'>Submit</button>" + "</form>" + "</body></html>");
        
        content.append("</body>" + "</html>");
        
        return content.toString();
    }

    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S404);
        response.setContent("404 Not Found");
    }
}
