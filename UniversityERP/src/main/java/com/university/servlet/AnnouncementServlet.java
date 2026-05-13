package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/announcements/*")
public class AnnouncementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            AnnouncementDAO dao = new AnnouncementDAO();
            String role = person.getRole();
            List<Map<String,Object>> list;

            if ("student".equals(role)) {
                Student st = new StudentDAO().getByPersonId(person.getPersonId());
                list = dao.getForStudent(st.getStudentId());
            } else if ("instructor".equals(role)) {
                Map<String,Object> inst = new InstructorDAO().getByPersonId(person.getPersonId());
                int instructorId = inst != null ? (int) inst.get("instructorId") : 0;
                list = dao.getForInstructor(instructorId);
            } else {
                list = dao.getAll();
            }
            res.getWriter().write(JsonUtil.toJson(list));
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
            String title      = (String) body.get("title");
            String content    = (String) body.get("content");
            String targetRole = (String) body.getOrDefault("targetRole","all");
            Integer offeringId = body.containsKey("offeringId") && !body.get("offeringId").toString().equals("-1")
                ? Integer.parseInt(body.get("offeringId").toString()) : null;
            boolean ok = new AnnouncementDAO().create(title, content, person.getPersonId(), targetRole, offeringId);
            res.getWriter().write(ok ? JsonUtil.ok("Announcement posted") : JsonUtil.error("Failed"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        try {
            int id = Integer.parseInt(path.substring(1));
            res.getWriter().write(new AnnouncementDAO().delete(id) ? JsonUtil.ok("Deleted") : JsonUtil.error("Failed"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
