package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;

public class XSDDBDAO implements XSDDAO {

	String DB_URL, DB_PASSWORD, DB_USER;

	public XSDDBDAO(String dbUrl, String dbUser, String dbPassword) {
		this.DB_URL = dbUrl;
		this.DB_USER = dbUser;
		this.DB_PASSWORD = dbPassword;
	}
	
    @Override
    public void addSchema(String uuid, String content) {
        String query = "INSERT INTO XSD (uuid, content) VALUES (?, ?)";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);
            statement.setString(2, content);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching XSD schema.", e);
        }
    }

    @Override
    public boolean containsSchema(String uuid) {
        boolean contains = false;
        String query = "SELECT `uuid` FROM XSD WHERE `uuid`=?";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                try {
                    if (result.getString("uuid").equals(uuid))
                        contains = true;
                } catch (SQLException e) {
                    contains = false;
                }
            }

        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching XSD schema.", e);
        }

        return contains;
    }

    @Override
    public void deleteSchema(String uuid) {
        String query = "DELETE FROM XSD WHERE uuid = ?";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while deleting XSD schema.", e);
        }
    }

    @Override
    public String getSchema(String uuid) {
        String query = "SELECT content FROM XSD WHERE uuid = ?";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

        	 statement.setString(1, uuid);
             try (ResultSet result = statement.executeQuery()) {
                 if (result.next()) {
                     return result.getString("content");
                 } else {
                     return null;
                 }
             }
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching XSD schema.", e);
        }
    }

    @Override
    public List<String> listSchemas() {
        List<String> schemas = new ArrayList<>();
        String query = "SELECT uuid FROM XSD";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                String uuid = result.getString("uuid");
                schemas.add(uuid);
            }

        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching XSD schema.", e);
        }

        return schemas;
    }
}
