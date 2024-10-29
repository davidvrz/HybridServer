package es.uvigo.esei.dai.hybridserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.controller.ResourceController;
import es.uvigo.esei.dai.hybridserver.http.HTTPParseException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;
import es.uvigo.esei.dai.hybridserver.model.PagesDBDAO;

public class HybridServerThread implements Runnable {
	private final Socket socket;
	private final ResourceController controller;

    public HybridServerThread(Socket clientSocket) {
        this.socket = clientSocket;
        this.controller = new ResourceController();
    }

    @Override
    public void run() {
    	try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
    			
    		HTTPRequest request = new HTTPRequest(reader);
		    HTTPResponse response = new HTTPResponse();

            switch(request.getMethod()) {
                case GET:
                    controller.handleGetRequest(request, response);
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

	        response.print(writer);
	        writer.flush();
		    
    	} catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
            e.printStackTrace(); 
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace(); 
        } catch (HTTPParseException e) {
        	System.out.println("HTTPParseException: " + e.getMessage());
			e.printStackTrace();
		} 
    }
}
