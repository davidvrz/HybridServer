package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import connection.DatabaseConnection;

public class PagesDBDAO {
	
    public String savePage(String htmlContent) {
        String insertSQL = "INSERT INTO Pages (uuid, content) VALUES (?, ?)";
        String uuid = null;
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {
             
            uuid = java.util.UUID.randomUUID().toString(); 
            statement.setString(1, uuid); 
            statement.setString(2, htmlContent); 
            
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        
        return uuid;
    }

    public boolean hasPages() {
        String query = "SELECT COUNT(*) FROM Pages"; 

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
             
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; 
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        
        return false; 
    }

    public String getPageByUUID(String uuid) {
        String query = "SELECT content FROM Pages WHERE uuid = ?";
        String content = null;

        try (Connection connection = DatabaseConnection.getConnection(); 
             PreparedStatement statement = connection.prepareStatement(query)) {
             
            statement.setString(1, uuid); 
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                content = resultSet.getString("content"); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return content; 
    }

    public List<String> listPages() {
        List<String> pages = new ArrayList<>();
        String query = "SELECT uuid, content FROM Pages";

        try (Connection connection = DatabaseConnection.getConnection(); 
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
             
            while (resultSet.next()) {
                String uuid = resultSet.getString("uuid"); 
                String content = resultSet.getString("content");
                pages.add(uuid); // Tambien se puede añadir otros campos
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }

        return pages;
    }
}