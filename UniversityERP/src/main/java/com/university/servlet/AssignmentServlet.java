package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/assignments/*")
public class AssignmentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            AssignmentDAO dao = new AssignmentDAO();

            if ("/student".equals(path)) {
                // Student: get all assignments across enrolled courses
                Student st = new StudentDAO().getByPersonId(person.getPersonId());
                res.getWriter().write(JsonUtil.toJson(dao.getByStudent(st.getStudentId())));

            } else if (path != null && path.startsWith("/offering/")) {
                // Instructor: get assignments for a specific offering
                int offeringId = Integer.parseInt(path.substring("/offering/".length()));
                res.getWriter().write(JsonUtil.toJson(dao.getByOffering(offeringId)));

            } else if (path != null && path.startsWith("/submissions/")) {
                // Instructor: get all submissions for an assignment
                int assignmentId = Integer.parseInt(path.substring("/submissions/".length()));
                res.getWriter().write(JsonUtil.toJson(dao.getSubmissions(assignmentId)));

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
        Person person = (Person) req.getSession().getAttribute("person");
        try {
            Map<String,Object> body = JsonUtil.parseBody(req);
            AssignmentDAO dao = new AssignmentDAO();

            if ("/submit".equals(path)) {
                // Student submits an assignment
                Student st = new StudentDAO().getByPersonId(person.getPersonId());
                int assignmentId = Integer.parseInt(body.get("assignmentId").toString());
                String text = (String) body.getOrDefault("submissionText", "");
                boolean ok = dao.submit(assignmentId, st.getStudentId(), text);
                res.getWriter().write(ok ? JsonUtil.ok("Submitted successfully") : JsonUtil.error("Submission failed"));

            } else if ("/grade".equals(path)) {
                // Instructor grades a submission
                int submissionId = Integer.parseInt(body.get("submissionId").toString());
                double marks = Double.parseDouble(body.get("marks").toString());
                String feedback = (String) body.getOrDefault("feedback", "");
                boolean ok = dao.grade(submissionId, marks, feedback);
                res.getWriter().write(ok ? JsonUtil.ok("Graded") : JsonUtil.error("Failed"));

            } else {
                // Instructor creates a new assignment
                int offeringId = Integer.parseInt(body.get("offeringId").toString());
                String title   = (String) body.get("title");
                String desc    = (String) body.getOrDefault("description", "");
                String due     = (String) body.get("dueDate");
                int maxMarks   = Integer.parseInt(body.getOrDefault("maxMarks","100").toString());
                boolean ok = dao.create(offeringId, title, desc, due, maxMarks);
                res.getWriter().write(ok ? JsonUtil.ok("Assignment created") : JsonUtil.error("Failed"));
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
            int id = Integer.parseInt(path.substring(1));
            res.getWriter().write(new AssignmentDAO().delete(id) ? JsonUtil.ok("Deleted") : JsonUtil.error("Failed"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }
}
