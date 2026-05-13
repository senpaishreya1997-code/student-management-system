package com.university.servlet;

import com.university.dao.DBConnection;
import com.university.model.Person;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;

@WebServlet("/api/offerings")
public class OfferingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        try {
            HttpSession session = req.getSession(false);
            Person person = (Person) session.getAttribute("person");

            String sql = """
                SELECT co.offering_id, co.section_no, co.semester, co.year, co.classroom, co.timings,
                       c.course_no, c.title AS course_title
                FROM CourseOffering co
                JOIN Course c ON co.course_id = c.course_id
                JOIN Instructor i ON co.instructor_id = i.instructor_id
                JOIN Employee e ON i.employee_id = e.employee_id
                WHERE e.person_id = ?
                ORDER BY co.year DESC, co.semester
            """;

            StringBuilder sb = new StringBuilder("[");
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, person.getPersonId());
                ResultSet rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{")
                      .append("\"offeringId\":").append(rs.getInt("offering_id")).append(",")
                      .append("\"sectionNo\":\"").append(rs.getString("section_no")).append("\",")
                      .append("\"semester\":\"").append(rs.getString("semester")).append("\",")
                      .append("\"year\":").append(rs.getInt("year")).append(",")
                      .append("\"classroom\":\"").append(safe(rs.getString("classroom"))).append("\",")
                      .append("\"courseNo\":\"").append(rs.getString("course_no")).append("\",")
                      .append("\"courseTitle\":\"").append(safe(rs.getString("course_title"))).append("\"")
                      .append("}");
                }
            }
            sb.append("]");
            res.getWriter().write(sb.toString());
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
