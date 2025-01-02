package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;

public class XMLDBDAO implements XMLDAO {
	
    @Override
    public void addDocument(String uuid, String content) {
        String query = "INSERT INTO XML (uuid, content) VALUES (?, ?)";
        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            statement.setString(2, content);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JDBCException("Error adding XML document to database.", e);
        }
    }

    @Override
    public boolean containsDocument(String uuid) {
        String query = "SELECT uuid FROM XML WHERE uuid = ?";
        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new JDBCException("Error checking if XML document exists in database.", e);
        }
    }

    @Override
    public void deleteDocument(String uuid) {
        String query = "DELETE FROM XML WHERE uuid = ?";
        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JDBCException("Error deleting XML document from database.", e);
        }
    }

    @Override
    public String getDocument(String uuid) {
        String query = "SELECT content FROM XML WHERE uuid = ?";
        
        try (Connection connection = JDBCConnection.getConnection();
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
            throw new JDBCException("Error fetching XML document from database.", e);
        }
    }

    @Override
    public List<String> listDocuments() {
        List<String> documents = new ArrayList<>();
        String query = "SELECT uuid FROM XML";
        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                documents.add(result.getString("uuid"));
            }
        } catch (SQLException e) {
            throw new JDBCException("Error listing XML documents from database.", e);
        }
        return documents;
    }
}
