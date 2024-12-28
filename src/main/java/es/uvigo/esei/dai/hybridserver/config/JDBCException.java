package es.uvigo.esei.dai.hybridserver.config;

public class JDBCException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
    public JDBCException(String message, Throwable cause) {
        super(message, cause);
    }
}