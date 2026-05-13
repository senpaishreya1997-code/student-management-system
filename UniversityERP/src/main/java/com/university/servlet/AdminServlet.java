package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        try {
            if ("/stats".equals(path)) {
                res.getWriter().write(JsonUtil.toJson(new EnrollmentDAO().getAdminStats()));
            } else if ("/instructors".equals(path)) {
                res.getWriter().write(JsonUtil.toJson(new InstructorDAO().getAll()));
            } else if (path != null && path.startsWith("/instructors/")) {
                int id = Integer.parseInt(path.substring("/instructors/".length()));
                Map<String,Object> inst = new InstructorDAO().getByInstructorId(id);
                res.getWriter().write(inst != null ? JsonUtil.toJson(inst) : JsonUtil.error("Not found"));
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
        String path = req.getPathInfo();
        try {
            Map<String,Object> body = JsonUtil.parseBody(req);
            if ("/instructors".equals(path)) {
                boolean ok = new InstructorDAO().addInstructor(
                    (String) body.get("fname"),
                    (String) body.getOrDefault("mname", ""),
                    (String) body.get("lname"),
                    (String) body.get("email"),
                    (String) body.getOrDefault("phone", ""),
                    (String) body.getOrDefault("title", "Lecturer"),
                    Integer.parseInt(body.getOrDefault("deptId", "1").toString()),
                    Integer.parseInt(body.getOrDefault("experience", "0").toString()),
                    Double.parseDouble(body.getOrDefault("salary", "50000").toString()),
                    (String) body.getOrDefault("joinDate", "2024-01-01"),
                    (String) body.getOrDefault("password", "faculty123")
                );
                res.getWriter().write(ok ? JsonUtil.ok("Instructor added") : JsonUtil.error("Failed"));
            } else {
                res.setStatus(400);
                res.getWriter().write(JsonUtil.error("Unknown path"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        try {
            Map<String,Object> body = JsonUtil.parseBody(req);
            if (path != null && path.startsWith("/instructors/")) {
                int id = Integer.parseInt(path.substring("/instructors/".length()));
                boolean ok = new InstructorDAO().updateInstructor(
                    id,
                    (String) body.get("fname"),
                    (String) body.get("lname"),
                    (String) body.getOrDefault("phone", ""),
                    (String) body.getOrDefault("title", ""),
                    Integer.parseInt(body.getOrDefault("deptId", "1").toString()),
                    Integer.parseInt(body.getOrDefault("experience", "0").toString())
                );
                res.getWriter().write(ok ? JsonUtil.ok("Instructor updated") : JsonUtil.error("Failed"));
            } else {
                res.setStatus(400);
                res.getWriter().write(JsonUtil.error("Unknown path"));
            }
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
            if (path != null && path.startsWith("/instructors/")) {
                int id = Integer.parseInt(path.substring("/instructors/".length()));
                boolean ok = new InstructorDAO().deleteInstructor(id);
                res.getWriter().write(ok ? JsonUtil.ok("Instructor deleted") : JsonUtil.error("Failed"));
            } else {
                res.setStatus(400);
                res.getWriter().write(JsonUtil.error("Unknown path"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
