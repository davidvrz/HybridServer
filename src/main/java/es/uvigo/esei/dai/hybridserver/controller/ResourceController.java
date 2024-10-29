package es.uvigo.esei.dai.hybridserver.controller;

import java.sql.SQLException;

import es.uvigo.esei.dai.hybridserver.config.JDBCException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.model.PagesDBDAO;



public class ResourceController {
    private PagesDBDAO pagesDAO;

    public ResourceController() {
        this.pagesDAO = new PagesDBDAO();
    }

    public void handleGetRequest(HTTPRequest request, HTTPResponse response) {
        String resource = request.getResourceName();
        String uuid = request.getParameter("uuid");

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
        String resource = request.getResourceName();

        switch (resource) {
            case "html":
                handleHtmlPost(request, response);
                break;
            default:
                handleNotFound(response);
                break;
        }
    }
    
    public void handleDeleteRequest(HTTPRequest request,HTTPResponse response){
    	System.out.println("Handling DELETE request");
        String resource = request.getResourceName();
        String uuid = request.getParameter("uuid");
        
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
                    response.setContent(pageContent);
                } else {
                    response.setStatus(HTTPResponseStatus.S404);
                    response.setContent("404 Not Found - Page not found for given UUID");
                }
            } else {
                response.setStatus(HTTPResponseStatus.S200);
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
            String htmlContent = request.getContent();
            
            if (htmlContent != null && !htmlContent.isEmpty()) {
                String newUUID = pagesDAO.savePage(htmlContent);
                response.setStatus(HTTPResponseStatus.S200);
                response.setContent("<html><body><p>Nueva página añadida con UUID: <a href='http://localhost:2000/html?uuid=" 
                                    + newUUID + "'>" + newUUID + "</a></p></body></html>");
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
                response.setStatus(HTTPResponseStatus.S200);
                response.setContent("<html><body><p>Página con UUID: " + uuid + " eliminada exitosamente.</p></body></html>");
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
        response.setStatus(HTTPResponseStatus.S200);
        response.setContent("<html><body>"
                + "<h1>Bienvenido a Hybrid Server</h1>"
                + "<h2>Autores:</h2>"
                + "<ul>"
                + "<li>David Álvarez Iglesias</li>"
                + "<li>Antonio Caride Pernas</li>"
                + "</ul>"
                + "<p><a href='/html'>Ver listado de páginas</a></p>"
                + "</body></html>");
    }
    
    private String generateHtmlPageHome() {
        StringBuilder content = new StringBuilder("<html><body><h1>Listado de Páginas</h1><ul>");

        for (String pageUUID : pagesDAO.listPages()) {
            content.append("<li>UUID: <a href='http://localhost:2000/html?uuid=")
                   .append(pageUUID)	
                   .append("'>").append(pageUUID).append("</a></li>");
        }
        content.append("</ul>");

        content.append("<h2>Añadir nueva página</h2>")
               .append("<form action='/html' method='POST'>")
               .append("<textarea name='html'></textarea>")
               .append("<button type='submit'>Submit</button>")
               .append("</form>")
               .append("</body></html>");
        
        return content.toString();
    }

    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S404);
        response.setContent("404 Not Found");
    }
}
