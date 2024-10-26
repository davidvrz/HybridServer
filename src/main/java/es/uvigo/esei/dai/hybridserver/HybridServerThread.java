package es.uvigo.esei.dai.hybridserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;

public class HybridServerThread implements Runnable {
	private final Socket socket;

    public HybridServerThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
    	try (Socket clientSocket = socket) {
            //String requestLine = readRequest(clientSocket);
    		BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    		PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    		
		    HTTPRequest request = new HTTPRequest(reader);
		    HTTPResponse response = new HTTPResponse();
		
		    // Configurar la respuesta
		    String resourceName = request.getResourceChain();
		    /*
		    if (pages.containsKey(resourceName)) {
		        response.setStatus(HTTPResponseStatus.S200);
		        response.setContent(pages.get(resourceName));
		    } else {
		        response.setStatus(HTTPResponseStatus.S404);
		        response.setContent("404 Not Found");
		    }
			*/
		    // Escribir la respuesta HTTP
		    response.print(writer);
	
	} catch (IOException | HTTPParseException e) {
	    e.printStackTrace();
	    // Responder con un error interno en caso de excepción
	    try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
	        HTTPResponse errorResponse = new HTTPResponse();
	        errorResponse.setStatus(HTTPResponseStatus.S500);
	        errorResponse.setContent("500 Internal Server Error");
	        errorResponse.print(writer);
	    } catch (IOException ioException) {
	        ioException.printStackTrace();
	    }
	        }
	    }

  private void initializeDefaultPages() {
      // Inicializar algunas páginas predeterminadas
	  pages.put("/", "<html><body><h1>Hybrid Server</h1><p>David Álvarez Iglesias\nAntonio Caride Pernas</p></body></html>");
      pages.put("/about", "<html><body><h1>Acerca de</h1><p>Este es un servidor HTTP simple construido en Java.</p></body></html>");
  }
    }
    
    private String readRequest(Socket clientSocket) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        // Leer la primera línea de la solicitud (la línea de solicitud)
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Empty request line");
        }
        requestBuilder.append(requestLine).append("\r\n");

        // Leer los encabezados de la solicitud
        String headerLine;
        while (!(headerLine = reader.readLine()).isEmpty()) {
            requestBuilder.append(headerLine).append("\r\n");
        }

        // Retornar la solicitud completa
        return requestBuilder.toString();
    }
}
