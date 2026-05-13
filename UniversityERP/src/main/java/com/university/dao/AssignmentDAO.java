package com.university.dao;

import java.sql.*;
import java.util.*;

public class AssignmentDAO {

    // ─── INSTRUCTOR: create assignment ───────────────────────────────────────
    public boolean create(int offeringId, String title, String description,
                          String dueDate, int maxMarks) throws SQLException {
        String sql = "INSERT INTO Assignment (offering_id,title,description,due_date,max_marks) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ps.setString(2, title);
            ps.setString(3, description);
            ps.setString(4, dueDate);
            ps.setInt(5, maxMarks);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int assignmentId) throws SQLException {
        String sql = "DELETE FROM Assignment WHERE assignment_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Get assignments for a specific offering ──────────────────────────────
    public List<Map<String,Object>> getByOffering(int offeringId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT a.assignment_id, a.title, a.description, a.due_date, a.max_marks,
                   a.created_at,
                   (SELECT COUNT(*) FROM AssignmentSubmission s WHERE s.assignment_id=a.assignment_id) AS submission_count
            FROM Assignment a
            WHERE a.offering_id=?
            ORDER BY a.due_date
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("assignmentId", rs.getInt("assignment_id"));
                m.put("title", rs.getString("title"));
                m.put("description", rs.getString("description"));
                m.put("dueDate", rs.getString("due_date"));
                m.put("maxMarks", rs.getInt("max_marks"));
                m.put("createdAt", rs.getString("created_at"));
                m.put("submissionCount", rs.getInt("submission_count"));
                list.add(m);
            }
        }
        return list;
    }

    // ─── Get all assignments for a student (across all enrollments) ───────────
    public List<Map<String,Object>> getByStudent(int studentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT a.assignment_id, a.title, a.description, a.due_date, a.max_marks,
                   c.course_no, c.title AS course_title,
                   sub.submission_id, sub.status AS sub_status,
                   sub.marks_obtained, sub.submitted_at, sub.feedback
            FROM Assignment a
            JOIN CourseOffering co ON a.offering_id = co.offering_id
            JOIN Course c ON co.course_id = c.course_id
            JOIN Enrollment e ON e.offering_id = co.offering_id AND e.student_id = ?
            LEFT JOIN AssignmentSubmission sub ON sub.assignment_id = a.assignment_id AND sub.student_id = ?
            WHERE e.status = 'enrolled'
            ORDER BY a.due_date
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("assignmentId", rs.getInt("assignment_id"));
                m.put("title", rs.getString("title"));
                m.put("description", rs.getString("description"));
                m.put("dueDate", rs.getString("due_date"));
                m.put("maxMarks", rs.getInt("max_marks"));
                m.put("courseNo", rs.getString("course_no"));
                m.put("courseTitle", rs.getString("course_title"));
                m.put("submissionId", rs.getObject("submission_id") != null ? rs.getInt("submission_id") : -1);
                m.put("subStatus", rs.getString("sub_status") != null ? rs.getString("sub_status") : "not_submitted");
                m.put("marksObtained", rs.getObject("marks_obtained") != null ? rs.getDouble("marks_obtained") : -1);
                m.put("submittedAt", rs.getString("submitted_at") != null ? rs.getString("submitted_at") : "");
                m.put("feedback", rs.getString("feedback") != null ? rs.getString("feedback") : "");
                list.add(m);
            }
        }
        return list;
    }

    // ─── Student submits assignment ───────────────────────────────────────────
    public boolean submit(int assignmentId, int studentId, String text) throws SQLException {
        String sql = """
            INSERT INTO AssignmentSubmission (assignment_id, student_id, submission_text, status)
            VALUES (?,?,?,'submitted')
            ON DUPLICATE KEY UPDATE submission_text=VALUES(submission_text),
            submitted_at=NOW(), status='submitted'
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ps.setInt(2, studentId);
            ps.setString(3, text);
            return ps.executeUpdate() > 0;
        }
    }

    // ─── Instructor: get all submissions for an assignment ────────────────────
    public List<Map<String,Object>> getSubmissions(int assignmentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT sub.submission_id, sub.submission_text, sub.submitted_at,
                   sub.marks_obtained, sub.feedback, sub.status,
                   p.fname, p.lname, s.enrollment_no, s.student_id
            FROM AssignmentSubmission sub
            JOIN Student s ON sub.student_id = s.student_id
            JOIN Person p ON s.person_id = p.person_id
            WHERE sub.assignment_id = ?
            ORDER BY sub.submitted_at
        """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("submissionId", rs.getInt("submission_id"));
                m.put("submissionText", rs.getString("submission_text") != null ? rs.getString("submission_text") : "");
                m.put("submittedAt", rs.getString("submitted_at"));
                m.put("marksObtained", rs.getObject("marks_obtained") != null ? rs.getDouble("marks_obtained") : -1);
                m.put("feedback", rs.getString("feedback") != null ? rs.getString("feedback") : "");
                m.put("status", rs.getString("status"));
                m.put("studentName", rs.getString("fname") + " " + rs.getString("lname"));
                m.put("enrollmentNo", rs.getString("enrollment_no"));
                m.put("studentId", rs.getInt("student_id"));
                list.add(m);
            }
        }
        return list;
    }

    // ─── Instructor grades a submission ───────────────────────────────────────
    public boolean grade(int submissionId, double marks, String feedback) throws SQLException {
        String sql = "UPDATE AssignmentSubmission SET marks_obtained=?, feedback=?, status='graded' WHERE submission_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, marks);
            ps.setString(2, feedback);
            ps.setInt(3, submissionId);
            return ps.executeUpdate() > 0;
        }
    }
}
