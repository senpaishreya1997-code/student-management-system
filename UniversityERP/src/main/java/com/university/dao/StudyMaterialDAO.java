package com.university.dao;

import java.sql.*;
import java.util.*;

public class StudyMaterialDAO {

    public boolean add(int offeringId, String title, String description,
                       String type, String url) throws SQLException {
        String sql = "INSERT INTO StudyMaterial (offering_id,title,description,material_type,url) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, type);
            ps.setString(5, url);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int materialId) throws SQLException {
        String sql = "DELETE FROM StudyMaterial WHERE material_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, materialId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String,Object>> getByOffering(int offeringId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT material_id, title, description, material_type, url, uploaded_at
            FROM StudyMaterial WHERE offering_id=? ORDER BY uploaded_at DESC
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("materialId",   rs.getInt("material_id"));
                m.put("title",        rs.getString("title"));
                m.put("description",  rs.getString("description") != null ? rs.getString("description") : "");
                m.put("type",         rs.getString("material_type"));
                m.put("url",          rs.getString("url") != null ? rs.getString("url") : "");
                m.put("uploadedAt",   rs.getString("uploaded_at"));
                list.add(m);
            }
        }
        return list;
    }

    // Student sees materials across all their enrolled offerings
    public List<Map<String,Object>> getByStudent(int studentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT sm.material_id, sm.title, sm.description, sm.material_type, sm.url, sm.uploaded_at,
                   c.course_no, c.title AS course_title
            FROM StudyMaterial sm
            JOIN CourseOffering co ON sm.offering_id = co.offering_id
            JOIN Course c ON co.course_id = c.course_id
            JOIN Enrollment e ON e.offering_id = co.offering_id
            WHERE e.student_id=? AND e.status='enrolled'
            ORDER BY sm.uploaded_at DESC
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("materialId",   rs.getInt("material_id"));
                m.put("title",        rs.getString("title"));
                m.put("description",  rs.getString("description") != null ? rs.getString("description") : "");
                m.put("type",         rs.getString("material_type"));
                m.put("url",          rs.getString("url") != null ? rs.getString("url") : "");
                m.put("uploadedAt",   rs.getString("uploaded_at"));
                m.put("courseNo",     rs.getString("course_no"));
                m.put("courseTitle",  rs.getString("course_title"));
                list.add(m);
            }
        }
        return list;
    }
}
