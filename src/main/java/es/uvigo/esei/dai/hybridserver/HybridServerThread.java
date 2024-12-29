package es.uvigo.esei.dai.hybridserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import es.uvigo.esei.dai.hybridserver.controller.HTMLController;
import es.uvigo.esei.dai.hybridserver.controller.XMLController;
import es.uvigo.esei.dai.hybridserver.controller.XSDController;
import es.uvigo.esei.dai.hybridserver.controller.XSLTController;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.http.MIME;

public class HybridServerThread implements Runnable {
	private final Socket socket;
	private final HTMLController htmlController;
	private final XMLController xmlController;
	private final XSDController xsdController;
	private final XSLTController xsltController;

    public HybridServerThread(Socket clientSocket, HTMLController htmlController, XMLController xmlController, XSDController xsdController, XSLTController xsltController) {
        this.socket = clientSocket;
        this.htmlController = htmlController;
        this.xmlController = xmlController;
        this.xsdController = xsdController;
        this.xsltController = xsltController;
    }

    @Override
    public void run() {
    	int port = socket.getLocalPort();
    	try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    			OutputStream output = socket.getOutputStream()) {
    			
    		HTTPRequest request = new HTTPRequest(reader);
		    HTTPResponse response = new HTTPResponse();

		    String resource = request.getResourceName();
        	String uuid = request.getResourceParameters().get("uuid");
        	
        	System.out.println(request.toString());
        	System.out.println(resource);
		    
	        switch (request.getMethod()) {
            case GET:
                switch (resource) {
                    case "html":
                        htmlController.handleHtmlGet(uuid, response, port);
                        break;
                    case "xml":
                        xmlController.handleXmlGet(uuid, response, port);
                        break;
                    case "xsd":
                        xsdController.handleXsdGet(uuid, response, port);
                        break;
                    case "xslt":
                        xsltController.handleXsltGet(uuid, response, port);
                        break;
                    case "":
                        handleWelcomePage(response, port);
                        break;
                    default:
                        handleNotFound(response);
                        break;
                }
                break;

            case POST:
                switch (resource) {
                    case "html":
                        htmlController.handleHtmlPost(request, response);
                        break;
                    case "xml":
                        xmlController.handleXmlPost(request, response);
                        break;
                    case "xsd":
                        xsdController.handleXsdPost(request, response);
                        break;
                    case "xslt":
                        xsltController.handleXsltPost(request, response);
                        break;
                    default:
                        handleNotFound(response);
                        break;
                }
                break;

            case DELETE:
                switch (resource) {
                    case "html":
                        htmlController.handleHtmlDelete(uuid, response);
                        break;
                    case "xml":
                        xmlController.handleXmlDelete(uuid, response);
                        break;
                    case "xsd":
                        xsdController.handleXsdDelete(uuid, response);
                        break;
                    case "xslt":
                        xsltController.handleXsltDelete(uuid, response);
                        break;
                    default:
                        handleNotFound(response);
                        break;
                }
                break;

            default:
                response.setStatus(HTTPResponseStatus.S405);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                response.setContent("405 Method Not Allowed");
                break;
        }
            
    		System.out.println(response.toString());

    		output.write(response.toString().getBytes());
		    
    	} catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
            e.printStackTrace(); 
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace(); 
        } catch (HTTPParseException e) {
        	System.out.println("HTTPParseException: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			e.printStackTrace();
		}
 
    }
    
    
    private void handleWelcomePage(HTTPResponse response, int port) {
        StringBuilder stringBuilder = new StringBuilder("<!DOCTYPE html>" + "<html lang='es'>" + "<head>"
                + "<meta charset='utf-8'/>" + "  <title>Hybrid Server</title>" + "</head>"
                + "<body>" + "<h1>Hybrid Server</h1>"
                + "<p>Autores: David Álvarez Iglesias, Antonio Caride Pernas.</p>"
                + "<ul>"
                + "<li><a href='http://localhost:" + port + "/html'>Lista de Páginas HTML</a></li>"
                + "<li><a href='http://localhost:" + port + "/xml'>Lista de Páginas XML</a></li>"
                + "<li><a href='http://localhost:" + port + "/xsd'>Lista de Páginas XSD</a></li>"
                + "<li><a href='http://localhost:" + port + "/xslt'>Lista de Páginas XSLT</a></li>"
                + "</ul>"
                + "</body>" + "</html>");

        response.setStatus(HTTPResponseStatus.S200);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
        response.setContent(stringBuilder.toString());
    }

    
    private void handleNotFound(HTTPResponse response) {
        response.setStatus(HTTPResponseStatus.S400);
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
        response.setContent("400 Bad Request");
    }
}
