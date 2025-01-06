package es.uvigo.esei.dai.hybridserver.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnection {

    public static Connection getConnection(String url, String user, String password) throws SQLException {
    	try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
            throw new SQLException("No se pudo cargar el controlador JDBC", e);
		}
        return DriverManager.getConnection(url, user, password);
    }
}
