package com.university.dao;

import java.sql.*;
import java.util.*;

public class EnrollmentDAO {

    public List<Map<String,Object>> getByStudent(int studentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, e.grade, e.grade_points, e.status, e.enrollment_date,
                   c.course_no, c.title AS course_title, c.credits,
                   co.semester, co.year, co.classroom, co.timings, co.section_no,
                   p.fname, p.lname
            FROM Enrollment e
            JOIN CourseOffering co ON e.offering_id = co.offering_id
            JOIN Course c ON co.course_id = c.course_id
            JOIN Instructor i ON co.instructor_id = i.instructor_id
            JOIN Employee emp ON i.employee_id = emp.employee_id
            JOIN Person p ON emp.person_id = p.person_id
            WHERE e.student_id = ?
            ORDER BY co.year DESC, co.semester
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("enrollmentId", rs.getInt("enrollment_id"));
                row.put("courseNo", rs.getString("course_no"));
                row.put("courseTitle", rs.getString("course_title"));
                row.put("credits", rs.getInt("credits"));
                row.put("semester", rs.getString("semester"));
                row.put("year", rs.getInt("year"));
                row.put("classroom", rs.getString("classroom"));
                row.put("timings", rs.getString("timings"));
                row.put("sectionNo", rs.getString("section_no"));
                row.put("instructorName", rs.getString("fname") + " " + rs.getString("lname"));
                row.put("grade", rs.getString("grade") != null ? rs.getString("grade") : "");
                row.put("gradePoints", rs.getObject("grade_points") != null ? rs.getDouble("grade_points") : -1);
                row.put("status", rs.getString("status"));
                row.put("enrollmentDate", rs.getString("enrollment_date"));
                list.add(row);
            }
        }
        return list;
    }

    public List<Map<String,Object>> getByOffering(int offeringId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT e.enrollment_id, e.grade, e.grade_points, e.status,
                   p.fname, p.lname, s.enrollment_no, s.student_id
            FROM Enrollment e
            JOIN Student s ON e.student_id = s.student_id
            JOIN Person p ON s.person_id = p.person_id
            WHERE e.offering_id = ?
            ORDER BY p.lname
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("enrollmentId", rs.getInt("enrollment_id"));
                row.put("studentId", rs.getInt("student_id"));
                row.put("studentName", rs.getString("fname") + " " + rs.getString("lname"));
                row.put("enrollmentNo", rs.getString("enrollment_no"));
                row.put("grade", rs.getString("grade") != null ? rs.getString("grade") : "");
                row.put("gradePoints", rs.getObject("grade_points") != null ? rs.getDouble("grade_points") : -1);
                row.put("status", rs.getString("status"));
                list.add(row);
            }
        }
        return list;
    }

    public boolean enroll(int studentId, int offeringId) throws SQLException {
        String sql = "INSERT INTO Enrollment (student_id, offering_id, enrollment_date) VALUES (?,?,CURDATE())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, offeringId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean drop(int enrollmentId) throws SQLException {
        String sql = "UPDATE Enrollment SET status='dropped' WHERE enrollment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGrade(int enrollmentId, String grade, double gradePoints) throws SQLException {
        String sql = "UPDATE Enrollment SET grade=?, grade_points=?, status='completed' WHERE enrollment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, grade);
            ps.setDouble(2, gradePoints);
            ps.setInt(3, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    public Map<String,Object> getStudentStats(int studentId) throws SQLException {
        Map<String,Object> stats = new LinkedHashMap<>();
        String sql = """
            SELECT
                COUNT(*) AS total,
                SUM(CASE WHEN status='enrolled' THEN 1 ELSE 0 END) AS active,
                SUM(CASE WHEN status='completed' THEN 1 ELSE 0 END) AS completed,
                SUM(CASE WHEN status='completed' THEN c.credits ELSE 0 END) AS total_credits,
                ROUND(SUM(CASE WHEN status='completed' AND grade_points IS NOT NULL THEN grade_points*c.credits ELSE 0 END) /
                      NULLIF(SUM(CASE WHEN status='completed' AND grade_points IS NOT NULL THEN c.credits ELSE 0 END),0), 2) AS cgpa
            FROM Enrollment e
            JOIN CourseOffering co ON e.offering_id=co.offering_id
            JOIN Course c ON co.course_id=c.course_id
            WHERE e.student_id=?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("active", rs.getInt("active"));
                stats.put("completed", rs.getInt("completed"));
                stats.put("totalCredits", rs.getInt("total_credits"));
                stats.put("cgpa", rs.getObject("cgpa") != null ? rs.getDouble("cgpa") : 0.0);
            }
        }
        return stats;
    }

    public Map<String,Object> getAdminStats() throws SQLException {
        Map<String,Object> stats = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            String s1 = "SELECT COUNT(*) AS cnt FROM Student";
            ResultSet r1 = conn.prepareStatement(s1).executeQuery();
            if (r1.next()) stats.put("totalStudents", r1.getInt("cnt"));

            String s2 = "SELECT COUNT(*) AS cnt FROM Course";
            ResultSet r2 = conn.prepareStatement(s2).executeQuery();
            if (r2.next()) stats.put("totalCourses", r2.getInt("cnt"));

            String s3 = "SELECT COUNT(*) AS cnt FROM Instructor";
            ResultSet r3 = conn.prepareStatement(s3).executeQuery();
            if (r3.next()) stats.put("totalInstructors", r3.getInt("cnt"));

            String s4 = "SELECT COUNT(*) AS cnt FROM Enrollment WHERE status='enrolled'";
            ResultSet r4 = conn.prepareStatement(s4).executeQuery();
            if (r4.next()) stats.put("totalEnrollments", r4.getInt("cnt"));

            String s5 = "SELECT COUNT(*) AS cnt FROM CourseOffering";
            ResultSet r5 = conn.prepareStatement(s5).executeQuery();
            if (r5.next()) stats.put("totalOfferings", r5.getInt("cnt"));
        }
        return stats;
    }
}
