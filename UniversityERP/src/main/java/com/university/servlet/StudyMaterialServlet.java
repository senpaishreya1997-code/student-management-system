package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/materials/*")
public class StudyMaterialServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            StudyMaterialDAO dao = new StudyMaterialDAO();

            if ("/student".equals(path)) {
                Student st = new StudentDAO().getByPersonId(person.getPersonId());
                res.getWriter().write(JsonUtil.toJson(dao.getByStudent(st.getStudentId())));

            } else if (path != null && path.startsWith("/offering/")) {
                int offeringId = Integer.parseInt(path.substring("/offering/".length()));
                res.getWriter().write(JsonUtil.toJson(dao.getByOffering(offeringId)));

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
        try {
            Map<String,Object> body = JsonUtil.parseBody(req);
            int offeringId = Integer.parseInt(body.get("offeringId").toString());
            String title   = (String) body.get("title");
            String desc    = (String) body.getOrDefault("description","");
            String type    = (String) body.getOrDefault("type","notes");
            String url     = (String) body.getOrDefault("url","");
            boolean ok = new StudyMaterialDAO().add(offeringId, title, desc, type, url);
            res.getWriter().write(ok ? JsonUtil.ok("Material added") : JsonUtil.error("Failed"));
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
            res.getWriter().write(new StudyMaterialDAO().delete(id) ? JsonUtil.ok("Deleted") : JsonUtil.error("Failed"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
