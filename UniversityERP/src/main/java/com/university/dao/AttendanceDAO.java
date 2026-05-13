package com.university.dao;

import java.sql.*;
import java.util.*;

public class AttendanceDAO {

    // ─── Instructor marks attendance for a date ───────────────────────────────
    public boolean mark(int offeringId, int studentId, String date,
                        String status, int markedBy) throws SQLException {
        String sql = """
            INSERT INTO Attendance (offering_id, student_id, attendance_date, status, marked_by)
            VALUES (?,?,?,?,?)
            ON DUPLICATE KEY UPDATE status=VALUES(status), marked_by=VALUES(marked_by)
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ps.setInt(2, studentId);
            ps.setString(3, date);
            ps.setString(4, status);
            ps.setInt(5, markedBy);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Get attendance for an offering on a specific date ────────────────────
    public List<Map<String,Object>> getByOfferingAndDate(int offeringId, String date) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT s.student_id, p.fname, p.lname, s.enrollment_no,
                   COALESCE(a.status,'not_marked') AS status,
                   a.attendance_date
            FROM Enrollment e
            JOIN Student s ON e.student_id = s.student_id
            JOIN Person p ON s.person_id = p.person_id
            LEFT JOIN Attendance a ON a.student_id=s.student_id
                AND a.offering_id=? AND a.attendance_date=?
            WHERE e.offering_id=? AND e.status='enrolled'
            ORDER BY p.lname
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ps.setString(2, date);
            ps.setInt(3, offeringId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("studentId", rs.getInt("student_id"));
                m.put("studentName", rs.getString("fname") + " " + rs.getString("lname"));
                m.put("enrollmentNo", rs.getString("enrollment_no"));
                m.put("status", rs.getString("status"));
                list.add(m);
            }
        }
        return list;
    }

    // ─── Get attendance summary for a student in an offering ─────────────────
    public Map<String,Object> getStudentSummary(int studentId, int offeringId) throws SQLException {
        Map<String,Object> m = new LinkedHashMap<>();
        String sql = """
            SELECT
                COUNT(*) AS total,
                SUM(CASE WHEN status='present' THEN 1 ELSE 0 END) AS present,
                SUM(CASE WHEN status='absent'  THEN 1 ELSE 0 END) AS absent,
                SUM(CASE WHEN status='late'    THEN 1 ELSE 0 END) AS late
            FROM Attendance
            WHERE student_id=? AND offering_id=?
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, offeringId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total   = rs.getInt("total");
                int present = rs.getInt("present");
                m.put("total",   total);
                m.put("present", present);
                m.put("absent",  rs.getInt("absent"));
                m.put("late",    rs.getInt("late"));
                m.put("percentage", total > 0 ? Math.round((present * 100.0) / total) : 0);
            }
        }
        return m;
    }

    // ─── Get full attendance record for a student across all enrollments ──────
    public List<Map<String,Object>> getByStudent(int studentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT c.course_no, c.title AS course_title,
                   COUNT(*) AS total,
                   SUM(CASE WHEN a.status='present' THEN 1 ELSE 0 END) AS present,
                   SUM(CASE WHEN a.status='absent'  THEN 1 ELSE 0 END) AS absent,
                   SUM(CASE WHEN a.status='late'    THEN 1 ELSE 0 END) AS late
            FROM Attendance a
            JOIN CourseOffering co ON a.offering_id = co.offering_id
            JOIN Course c ON co.course_id = c.course_id
            WHERE a.student_id=?
            GROUP BY co.offering_id, c.course_no, c.title
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                int total   = rs.getInt("total");
                int present = rs.getInt("present");
                m.put("courseNo",    rs.getString("course_no"));
                m.put("courseTitle", rs.getString("course_title"));
                m.put("total",   total);
                m.put("present", present);
                m.put("absent",  rs.getInt("absent"));
                m.put("late",    rs.getInt("late"));
                m.put("percentage", total > 0 ? Math.round((present * 100.0) / total) : 0);
                list.add(m);
            }
        }
        return list;
    }

    // ─── Get dates attendance was marked for an offering ─────────────────────
    public List<String> getMarkedDates(int offeringId) throws SQLException {
        List<String> dates = new ArrayList<>();
        String sql = "SELECT DISTINCT attendance_date FROM Attendance WHERE offering_id=? ORDER BY attendance_date DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) dates.add(rs.getString("attendance_date"));
        }
        return dates;
    }
}
