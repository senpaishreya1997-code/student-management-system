package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/instructor/*")
public class InstructorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            InstructorDAO dao = new InstructorDAO();
            if ("/me".equals(path)) {
                Map<String,Object> inst = dao.getByPersonId(person.getPersonId());
                res.getWriter().write(inst != null ? JsonUtil.toJson(inst) : JsonUtil.error("Not found"));
            } else if (path != null && path.startsWith("/students/")) {
                // Get all students enrolled in any offering of this instructor
                int instructorId = Integer.parseInt(path.substring("/students/".length()));
                res.getWriter().write(JsonUtil.toJson(getInstructorStudents(instructorId)));
            } else {
                res.setStatus(400);
                res.getWriter().write(JsonUtil.error("Invalid path"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    private List<Map<String,Object>> getInstructorStudents(int instructorId) throws Exception {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT DISTINCT s.student_id, p.fname, p.lname, s.enrollment_no, s.program,
                   e.enrollment_id, e.grade, e.grade_points, e.status,
                   c.course_no, c.title AS course_title,
                   co.offering_id, co.semester, co.year
            FROM Enrollment e
            JOIN Student s ON e.student_id = s.student_id
            JOIN Person p ON s.person_id = p.person_id
            JOIN CourseOffering co ON e.offering_id = co.offering_id
            JOIN Course c ON co.course_id = c.course_id
            WHERE co.instructor_id = ? AND e.status = 'enrolled'
            ORDER BY p.lname, c.course_no
        """;
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String,Object> m = new java.util.LinkedHashMap<>();
                m.put("studentId",    rs.getInt("student_id"));
                m.put("studentName",  rs.getString("fname") + " " + rs.getString("lname"));
                m.put("enrollmentNo", rs.getString("enrollment_no"));
                m.put("program",      rs.getString("program"));
                m.put("enrollmentId", rs.getInt("enrollment_id"));
                m.put("grade",        rs.getString("grade") != null ? rs.getString("grade") : "");
                m.put("gradePoints",  rs.getObject("grade_points") != null ? rs.getDouble("grade_points") : -1);
                m.put("status",       rs.getString("status"));
                m.put("courseNo",     rs.getString("course_no"));
                m.put("courseTitle",  rs.getString("course_title"));
                m.put("offeringId",   rs.getInt("offering_id"));
                m.put("semester",     rs.getString("semester"));
                m.put("year",         rs.getInt("year"));
                list.add(m);
            }
        }
        return list;
    }
}
