package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/attendance/*")
public class AttendanceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            AttendanceDAO dao = new AttendanceDAO();

            if ("/student".equals(path)) {
                // Student: see own attendance across all courses
                Student st = new StudentDAO().getByPersonId(person.getPersonId());
                res.getWriter().write(JsonUtil.toJson(dao.getByStudent(st.getStudentId())));

            } else if (path != null && path.startsWith("/dates/")) {
                // Instructor: get all dated sessions for an offering
                int offeringId = Integer.parseInt(path.substring("/dates/".length()));
                List<String> dates = dao.getMarkedDates(offeringId);
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < dates.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append("\"").append(dates.get(i)).append("\"");
                }
                res.getWriter().write(sb.append("]").toString());

            } else if (path != null && path.startsWith("/sheet/")) {
                // Instructor: get attendance sheet for offering on a date
                // path = /sheet/{offeringId}/{date}
                String[] parts = path.substring("/sheet/".length()).split("/");
                int offeringId = Integer.parseInt(parts[0]);
                String date = parts[1];
                res.getWriter().write(JsonUtil.toJson(dao.getByOfferingAndDate(offeringId, date)));

            } else {
                res.setStatus(400);
                res.getWriter().write(JsonUtil.error("Invalid path"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            Map<String,Object> body = JsonUtil.parseBody(req);
            // Body: { offeringId, date, records: [{studentId, status},...] }
            // Since our JSON parser is simple, records come pre-encoded
            // We handle bulk mark via a different approach:
            // The frontend sends one record at a time or sends a JSON array
            int offeringId = Integer.parseInt(body.get("offeringId").toString());
            int studentId  = Integer.parseInt(body.get("studentId").toString());
            String date    = (String) body.get("date");
            String status  = (String) body.getOrDefault("status", "present");
            boolean ok = new AttendanceDAO().mark(offeringId, studentId, date, status, person.getPersonId());
            res.getWriter().write(ok ? JsonUtil.ok("Marked") : JsonUtil.error("Failed"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
