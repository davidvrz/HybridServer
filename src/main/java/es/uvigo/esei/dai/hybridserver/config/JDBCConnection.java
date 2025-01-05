package es.uvigo.esei.dai.hybridserver.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {
	private static String url;
    private static String user;
    private static String password;

    public static void initialize(String dbUrl, String dbUser, String dbPassword) {
        url = dbUrl;
        user = dbUser;
        password = dbPassword;
    }

    public static Connection getConnection() throws SQLException {
    	return DriverManager.getConnection(url, user, password);
    }
    
}