package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;

public class HTMLDBDAO implements HTMLDAO{
	
	String DB_URL, DB_PASSWORD, DB_USER;

	public HTMLDBDAO(String dbUrl, String dbUser, String dbPassword) {
		this.DB_URL = dbUrl;
		this.DB_USER = dbUser;
		this.DB_PASSWORD = dbPassword;
	}
	
	@Override
    public void addDocument(String uuid, String content) {
        String query = "INSERT INTO HTML (uuid, content) VALUES (?, ?)";
        
        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

        	statement.setString(1, uuid); 
            statement.setString(2, content); 
            
            statement.executeUpdate();
        } catch (SQLException e) {
        	throw new JDBCException("Database error occurred while fetching document.", e); 
        }
    }
    
	@Override
    public boolean containsDocument(String uuid) {
		
		boolean contains = false;

		String query = "SELECT `uuid` FROM HTML WHERE `uuid`=?";
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
			throw new JDBCException("Database error occurred while fetching document.", e);
		}
	
		return contains;
	}

	@Override
    public void deleteDocument(String uuid) {
        String query = "DELETE FROM HTML WHERE uuid = ?";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD); 
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid); 
            statement.executeUpdate();
            
        } catch (SQLException e) {
        	throw new JDBCException("Database error occurred while fetching document.", e);
        }
    }


	@Override
    public String getDocument(String uuid) {
        String query = "SELECT content FROM HTML WHERE uuid = ?";
        String content = "";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD); 
             PreparedStatement statement = connection.prepareStatement(query)) {
             
            statement.setString(1, uuid); 
            try (ResultSet result = statement.executeQuery()) {
				result.next();
				content = result.getString("content");
			}

        } catch (SQLException e) {
        	throw new JDBCException("Database error occurred while fetching document.", e);
        }

        return content; 
    }

	@Override
    public List<String> listDocuments() {
        List<String> documents = new ArrayList<>();
        String query = "SELECT uuid FROM HTML";

        try (Connection connection = JDBCConnection.getConnection(DB_URL, DB_USER, DB_PASSWORD); 
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {
             while (result.next()) {
                String uuid = result.getString("uuid"); 
                documents.add(uuid);
            }
        } catch (SQLException e) {
        	e.printStackTrace(); 
        	throw new JDBCException("Database error occurred while fetching document.", e); 
        }

        return documents;
    }
	
}