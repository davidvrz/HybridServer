package es.uvigo.esei.dai.hybridserver.config;

public class JDBCException extends RuntimeException {
    public JDBCException(String message, Throwable cause) {
        super(message, cause);
    }
}