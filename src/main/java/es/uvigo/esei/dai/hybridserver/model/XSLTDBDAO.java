package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;

public class XSLTDBDAO implements XSLTDAO {

	String DB_URL, DB_PASSWORD, DB_USER;

	public XSLTDBDAO(String dbUrl, String dbUser, String dbPassword) {
		this.DB_URL = dbUrl;
		this.DB_USER = dbUser;
		this.DB_PASSWORD = dbPassword;
	}
	
    @Override
    public void addStylesheet(String uuid, String xslt, String xsd) {
        String query = "INSERT INTO XSLT (uuid, content, xsd) VALUES (?, ?, ?)";
        
        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);
            statement.setString(2, xslt);
            statement.setString(3, xsd);
            
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while adding stylesheet.", e);
        }
    }

    @Override
    public boolean containsStylesheet(String uuid) {
        boolean contains = false;

        String query = "SELECT `uuid` FROM XSLT WHERE `uuid`=?";
        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
                
            statement.setString(1, uuid);
            
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    contains = true;
                }
            } 
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching stylesheet.", e);
        }

        return contains;
    }

    @Override
    public void deleteStylesheet(String uuid) {
        String query = "DELETE FROM XSLT WHERE uuid = ?";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while deleting stylesheet.", e);
        }
    }

    @Override
    public String getStylesheet(String uuid) {
        String query = "SELECT content FROM XSLT WHERE uuid = ?";

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
            throw new JDBCException("Database error occurred while fetching stylesheet.", e);
        }
    }

    @Override
    public List<String> listStylesheets() {
        List<String> stylesheets = new ArrayList<>();
        String query = "SELECT uuid FROM XSLT";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                String uuid = result.getString("uuid");
                stylesheets.add(uuid);
            }
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while listing stylesheets.", e);
        }

        return stylesheets;
    }

    @Override
    public String getXsd(String uuid) {
        String query = "SELECT xsd FROM XSLT WHERE uuid = ?";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
                
            statement.setString(1, uuid);
                        
            try (ResultSet result = statement.executeQuery()) {
            	if (result.next()) {
            		System.out.println(result.getString("xsd"));
                    return result.getString("xsd");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching XSD.", e);
        }
    }
}
