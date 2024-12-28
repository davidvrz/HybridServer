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

import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;

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

            switch(request.getMethod()) {
                case GET:
                    controller.handleGetRequest(request, response, port);
                    break;
                case POST:
                    controller.handlePostRequest(request, response);
                    break;
                case DELETE:
                    controller.handleDeleteRequest(request, response);
                    break;
                default:
                    response.setStatus(HTTPResponseStatus.S405);
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
}
