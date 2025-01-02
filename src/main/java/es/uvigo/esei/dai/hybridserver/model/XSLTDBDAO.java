package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.config.JDBCConnection;
import es.uvigo.esei.dai.hybridserver.config.JDBCException;

public class XSLTDBDAO implements XSLTDAO {

    @Override
    public void addStylesheet(String uuid, String xslt, String xsd) {
        String query = "INSERT INTO XSLT (uuid, xslt, xsd) VALUES (?, ?, ?)";
        
        try (Connection connection = JDBCConnection.getConnection();
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
        try (Connection connection = JDBCConnection.getConnection();
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

        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while deleting stylesheet.", e);
        }
    }

    @Override
    public String getStylesheet(String uuid) {
        String query = "SELECT xslt FROM XSLT WHERE uuid = ?";
        String xslt = "";

        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
                
            statement.setString(1, uuid);
            
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    xslt = result.getString("xslt");
                }
            }
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching stylesheet.", e);
        }

        return xslt;
    }

    @Override
    public List<String> listStylesheets() {
        List<String> stylesheets = new ArrayList<>();
        String query = "SELECT uuid FROM XSLT";

        try (Connection connection = JDBCConnection.getConnection();
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
        String xsd = "";

        try (Connection connection = JDBCConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
                
            statement.setString(1, uuid);
            
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    xsd = result.getString("xsd");
                }
            }
        } catch (SQLException e) {
            throw new JDBCException("Database error occurred while fetching XSD.", e);
        }

        return xsd;
    }
}
