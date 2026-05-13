package com.university.dao;

import com.university.model.Course;
import java.sql.*;
import java.util.*;

public class CourseDAO {

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = """
            SELECT c.*, d.dept_name FROM Course c
            LEFT JOIN Department d ON c.dept_id = d.dept_id ORDER BY c.course_no
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCourse(rs));
        }
        return list;
    }

    public boolean addCourse(Course c) throws SQLException {
        String sql = "INSERT INTO Course (course_no,title,credits,syllabus,dept_id) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCourseNo());
            ps.setString(2, c.getTitle());
            ps.setInt(3, c.getCredits());
            ps.setString(4, c.getSyllabus());
            ps.setInt(5, c.getDeptId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM Course WHERE course_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String,Object>> getAllOfferings() throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT co.offering_id, co.year, co.semester, co.section_no, co.classroom, co.timings, co.max_students,
                   c.course_id, c.course_no, c.title AS course_title, c.credits,
                   p.fname, p.lname, i.instructor_id,
                   (SELECT COUNT(*) FROM Enrollment e WHERE e.offering_id=co.offering_id AND e.status='enrolled') AS enrolled_count
            FROM CourseOffering co
            JOIN Course c ON co.course_id = c.course_id
            JOIN Instructor i ON co.instructor_id = i.instructor_id
            JOIN Employee emp ON i.employee_id = emp.employee_id
            JOIN Person p ON emp.person_id = p.person_id
            ORDER BY co.year DESC, co.semester, c.course_no
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("offeringId", rs.getInt("offering_id"));
                row.put("courseId", rs.getInt("course_id"));
                row.put("courseNo", rs.getString("course_no"));
                row.put("courseTitle", rs.getString("course_title"));
                row.put("credits", rs.getInt("credits"));
                row.put("year", rs.getInt("year"));
                row.put("semester", rs.getString("semester"));
                row.put("sectionNo", rs.getString("section_no"));
                row.put("classroom", rs.getString("classroom"));
                row.put("timings", rs.getString("timings"));
                row.put("maxStudents", rs.getInt("max_students"));
                row.put("enrolledCount", rs.getInt("enrolled_count"));
                row.put("instructorId", rs.getInt("instructor_id"));
                row.put("instructorName", rs.getString("fname") + " " + rs.getString("lname"));
                list.add(row);
            }
        }
        return list;
    }

    public List<Map<String,Object>> getOfferingsByInstructor(int instructorId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT co.offering_id, co.year, co.semester, co.section_no, co.classroom, co.timings,
                   c.course_no, c.title AS course_title, c.credits,
                   (SELECT COUNT(*) FROM Enrollment e WHERE e.offering_id=co.offering_id AND e.status='enrolled') AS enrolled_count
            FROM CourseOffering co
            JOIN Course c ON co.course_id = c.course_id
            WHERE co.instructor_id = ?
            ORDER BY co.year DESC, co.semester
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("offeringId", rs.getInt("offering_id"));
                row.put("courseNo", rs.getString("course_no"));
                row.put("courseTitle", rs.getString("course_title"));
                row.put("credits", rs.getInt("credits"));
                row.put("year", rs.getInt("year"));
                row.put("semester", rs.getString("semester"));
                row.put("sectionNo", rs.getString("section_no"));
                row.put("classroom", rs.getString("classroom"));
                row.put("timings", rs.getString("timings"));
                row.put("enrolledCount", rs.getInt("enrolled_count"));
                list.add(row);
            }
        }
        return list;
    }

    public boolean addOffering(int courseId, int instructorId, int year, String semester,
                               String sectionNo, String classroom, String timings, int maxStudents) throws SQLException {
        String sql = "INSERT INTO CourseOffering (course_id,instructor_id,year,semester,section_no,classroom,timings,max_students) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, instructorId);
            ps.setInt(3, year);
            ps.setString(4, semester);
            ps.setString(5, sectionNo);
            ps.setString(6, classroom);
            ps.setString(7, timings);
            ps.setInt(8, maxStudents);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteOffering(int offeringId) throws SQLException {
        String sql = "DELETE FROM CourseOffering WHERE offering_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offeringId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Map<String,Object>> getAvailableOfferingsForStudent(int studentId) throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT co.offering_id, co.year, co.semester, co.section_no, co.classroom, co.timings, co.max_students,
                   c.course_no, c.title AS course_title, c.credits,
                   p.fname, p.lname,
                   (SELECT COUNT(*) FROM Enrollment e2 WHERE e2.offering_id=co.offering_id AND e2.status='enrolled') AS enrolled_count
            FROM CourseOffering co
            JOIN Course c ON co.course_id = c.course_id
            JOIN Instructor i ON co.instructor_id = i.instructor_id
            JOIN Employee emp ON i.employee_id = emp.employee_id
            JOIN Person p ON emp.person_id = p.person_id
            WHERE co.offering_id NOT IN (
                SELECT offering_id FROM Enrollment WHERE student_id=? AND status != 'dropped'
            )
            ORDER BY c.course_no
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("offeringId", rs.getInt("offering_id"));
                row.put("courseNo", rs.getString("course_no"));
                row.put("courseTitle", rs.getString("course_title"));
                row.put("credits", rs.getInt("credits"));
                row.put("year", rs.getInt("year"));
                row.put("semester", rs.getString("semester"));
                row.put("sectionNo", rs.getString("section_no"));
                row.put("classroom", rs.getString("classroom"));
                row.put("timings", rs.getString("timings"));
                row.put("maxStudents", rs.getInt("max_students"));
                row.put("enrolledCount", rs.getInt("enrolled_count"));
                row.put("instructorName", rs.getString("fname") + " " + rs.getString("lname"));
                list.add(row);
            }
        }
        return list;
    }

    public List<Map<String,Object>> getDepartments() throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = "SELECT dept_id, dept_name, dept_code FROM Department ORDER BY dept_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("deptId", rs.getInt("dept_id"));
                row.put("deptName", rs.getString("dept_name"));
                row.put("deptCode", rs.getString("dept_code"));
                list.add(row);
            }
        }
        return list;
    }

    public List<Map<String,Object>> getInstructors() throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT i.instructor_id, p.fname, p.lname, i.title, d.dept_name
            FROM Instructor i
            JOIN Employee e ON i.employee_id = e.employee_id
            JOIN Person p ON e.person_id = p.person_id
            LEFT JOIN Department d ON e.dept_id = d.dept_id
            ORDER BY p.lname
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("instructorId", rs.getInt("instructor_id"));
                row.put("name", rs.getString("fname") + " " + rs.getString("lname"));
                row.put("title", rs.getString("title"));
                row.put("deptName", rs.getString("dept_name"));
                list.add(row);
            }
        }
        return list;
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setCourseNo(rs.getString("course_no"));
        c.setTitle(rs.getString("title"));
        c.setCredits(rs.getInt("credits"));
        c.setSyllabus(rs.getString("syllabus"));
        c.setDeptId(rs.getInt("dept_id"));
        c.setDeptName(rs.getString("dept_name"));
        return c;
    }
    public boolean updateOffering(int offeringId, int courseId, int instructorId, int year,
            String semester, String sectionNo, String classroom,
            String timings, int maxStudents) throws SQLException {
String sql = """
UPDATE CourseOffering
SET course_id=?, instructor_id=?, year=?, semester=?, section_no=?,
classroom=?, timings=?, max_students=?
WHERE offering_id=?
""";
try (Connection conn = DBConnection.getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setInt(1, courseId); ps.setInt(2, instructorId);
ps.setInt(3, year); ps.setString(4, semester); ps.setString(5, sectionNo);
ps.setString(6, classroom); ps.setString(7, timings);
ps.setInt(8, maxStudents); ps.setInt(9, offeringId);
return ps.executeUpdate() > 0;
}
}

public boolean updateCourse(int courseId, String courseNo, String title,
          int credits, String syllabus, int deptId) throws SQLException {
String sql = "UPDATE Course SET course_no=?, title=?, credits=?, syllabus=?, dept_id=? WHERE course_id=?";
try (Connection conn = DBConnection.getConnection();
PreparedStatement ps = conn.prepareStatement(sql)) {
ps.setString(1, courseNo); ps.setString(2, title);
ps.setInt(3, credits); ps.setString(4, syllabus);
ps.setInt(5, deptId); ps.setInt(6, courseId);
return ps.executeUpdate() > 0;
}
}
}
