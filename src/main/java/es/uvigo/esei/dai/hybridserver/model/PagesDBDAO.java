package es.uvigo.esei.dai.hybridserver.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PagesDBDAO {
	public void addPage(Page page) throws SQLException {
        String query = "INSERT INTO pages (uuid, content, created_date, last_modified_date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, page.getUuid().toString());
            stmt.setString(2, page.getContent());
            stmt.setTimestamp(3, Timestamp.valueOf(page.getCreatedDate()));
            stmt.setTimestamp(4, Timestamp.valueOf(page.getLastModifiedDate()));
            stmt.executeUpdate();
        }
    }

    // Método para obtener una página por su UUID
    public Page getPageById(UUID uuid) throws SQLException {
        String query = "SELECT * FROM pages WHERE uuid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Page(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("content")
                );
            }
            return null; // No se encontró la página
        }
    }

    // Método para obtener todas las páginas
    public List<Page> getAllPages() throws SQLException {
        List<Page> pages = new ArrayList<>();
        String query = "SELECT * FROM pages";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                pages.add(new Page(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("content")
                ));
            }
        }
        return pages;
    }

    // Método para actualizar el contenido de una página existente
    public void updatePage(Page page) throws SQLException {
        String query = "UPDATE pages SET content = ?, last_modified_date = ? WHERE uuid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, page.getContent());
            stmt.setTimestamp(2, Timestamp.valueOf(page.getLastModifiedDate()));
            stmt.setString(3, page.getUuid().toString());
            stmt.executeUpdate();
        }
    }

    // Método para eliminar una página por su UUID
    public void deletePage(UUID uuid) throws SQLException {
        String query = "DELETE FROM pages WHERE uuid = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        }
    }
}
