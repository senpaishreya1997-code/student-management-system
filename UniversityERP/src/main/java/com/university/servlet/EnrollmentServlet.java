package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/enrollments/*")
public class EnrollmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        try {
            EnrollmentDAO dao = new EnrollmentDAO();
            Person person = (Person) req.getSession().getAttribute("person");

            if (path != null && path.startsWith("/offering/")) {
                int offeringId = Integer.parseInt(path.substring("/offering/".length()));
                res.getWriter().write(JsonUtil.toJson(dao.getByOffering(offeringId)));
            } else if ("/stats".equals(path)) {
                StudentDAO sDao = new StudentDAO();
                Student st = sDao.getByPersonId(person.getPersonId());
                res.getWriter().write(JsonUtil.toJson(dao.getStudentStats(st.getStudentId())));
            } else {
                StudentDAO sDao = new StudentDAO();
                Student st = sDao.getByPersonId(person.getPersonId());
                res.getWriter().write(JsonUtil.toJson(dao.getByStudent(st.getStudentId())));
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
            Person person = (Person) req.getSession().getAttribute("person");
            StudentDAO sDao = new StudentDAO();
            Student st = sDao.getByPersonId(person.getPersonId());
            int offeringId = Integer.parseInt(body.get("offeringId").toString());
            boolean ok = new EnrollmentDAO().enroll(st.getStudentId(), offeringId);
            res.getWriter().write(ok ? JsonUtil.ok("Enrolled successfully") : JsonUtil.error("Already enrolled or failed"));
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
            EnrollmentDAO dao = new EnrollmentDAO();
            if (path != null && path.startsWith("/grade/")) {
                int enrollmentId = Integer.parseInt(path.substring("/grade/".length()));
                String grade = (String) body.get("grade");
                double gp = Double.parseDouble(body.get("gradePoints").toString());
                res.getWriter().write(dao.updateGrade(enrollmentId, grade, gp) ? JsonUtil.ok("Grade saved") : JsonUtil.error("Failed"));
            } else if (path != null && path.startsWith("/drop/")) {
                int enrollmentId = Integer.parseInt(path.substring("/drop/".length()));
                res.getWriter().write(dao.drop(enrollmentId) ? JsonUtil.ok("Dropped") : JsonUtil.error("Failed"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
