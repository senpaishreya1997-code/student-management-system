package com.university.dao;

import java.sql.*;
import java.util.*;

public class AnnouncementDAO {

    // ─── Create announcement ──────────────────────────────────────────────────
    public boolean create(String title, String content, int createdBy,
                          String targetRole, Integer offeringId) throws SQLException {
        String sql = "INSERT INTO Announcement (title,content,created_by,target_role,offering_id) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, createdBy);
            ps.setString(4, targetRole);
            if (offeringId != null) ps.setInt(5, offeringId);
            else ps.setNull(5, Types.INTEGER);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int announcementId) throws SQLException {
        String sql = "DELETE FROM Announcement WHERE announcement_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, announcementId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Get announcements visible to a student ───────────────────────────────
    public List<Map<String,Object>> getForStudent(int studentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT a.announcement_id, a.title, a.content, a.created_at,
                   a.target_role, a.offering_id,
                   p.fname, p.lname, p.role AS creator_role,
                   c.course_no, c.title AS course_title
            FROM Announcement a
            JOIN Person p ON a.created_by = p.person_id
            LEFT JOIN CourseOffering co ON a.offering_id = co.offering_id
            LEFT JOIN Course c ON co.course_id = c.course_id
            WHERE a.target_role IN ('all','student')
               OR (a.offering_id IN (
                   SELECT offering_id FROM Enrollment WHERE student_id=? AND status='enrolled'
               ))
            ORDER BY a.created_at DESC
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─── Get announcements for an instructor ──────────────────────────────────
    public List<Map<String,Object>> getForInstructor(int instructorId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT a.announcement_id, a.title, a.content, a.created_at,
                   a.target_role, a.offering_id,
                   p.fname, p.lname, p.role AS creator_role,
                   c.course_no, c.title AS course_title
            FROM Announcement a
            JOIN Person p ON a.created_by = p.person_id
            LEFT JOIN CourseOffering co ON a.offering_id = co.offering_id
            LEFT JOIN Course c ON co.course_id = c.course_id
            WHERE a.target_role IN ('all','instructor')
               OR a.offering_id IN (
                   SELECT offering_id FROM CourseOffering WHERE instructor_id=?
               )
            ORDER BY a.created_at DESC
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ─── Get all announcements (admin view) ───────────────────────────────────
    public List<Map<String,Object>> getAll() throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT a.announcement_id, a.title, a.content, a.created_at,
                   a.target_role, a.offering_id,
                   p.fname, p.lname, p.role AS creator_role,
                   c.course_no, c.title AS course_title
            FROM Announcement a
            JOIN Person p ON a.created_by = p.person_id
            LEFT JOIN CourseOffering co ON a.offering_id = co.offering_id
            LEFT JOIN Course c ON co.course_id = c.course_id
            ORDER BY a.created_at DESC
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private Map<String,Object> mapRow(ResultSet rs) throws SQLException {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("announcementId", rs.getInt("announcement_id"));
        m.put("title",        rs.getString("title"));
        m.put("content",      rs.getString("content"));
        m.put("createdAt",    rs.getString("created_at"));
        m.put("targetRole",   rs.getString("target_role"));
        m.put("offeringId",   rs.getObject("offering_id") != null ? rs.getInt("offering_id") : -1);
        m.put("creatorName",  rs.getString("fname") + " " + rs.getString("lname"));
        m.put("creatorRole",  rs.getString("creator_role"));
        m.put("courseNo",     rs.getString("course_no") != null ? rs.getString("course_no") : "");
        m.put("courseTitle",  rs.getString("course_title") != null ? rs.getString("course_title") : "");
        return m;
    }
}
