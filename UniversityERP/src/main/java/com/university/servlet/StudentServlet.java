package com.university.servlet;

import com.university.dao.*;
import com.university.model.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet("/api/students/*")
public class StudentServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        try {
            StudentDAO dao = new StudentDAO();
            if ("/me".equals(path)) {
                Person p = (Person) req.getSession().getAttribute("person");
                Student s = dao.getByPersonId(p.getPersonId());
                if (s != null) res.getWriter().write(studentToJson(s));
                else res.getWriter().write(JsonUtil.error("Student record not found"));
            } else if ("/stats".equals(path)) {
                Person p = (Person) req.getSession().getAttribute("person");
                Student s = dao.getByPersonId(p.getPersonId());
                if (s == null) { res.getWriter().write("{}"); return; }
                Map<String,Object> stats = new EnrollmentDAO().getStudentStats(s.getStudentId());
                res.getWriter().write(JsonUtil.toJson(stats));
            } else {
                List<Student> students = dao.getAllStudents();
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < students.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(studentToJson(students.get(i)));
                }
                res.getWriter().write(sb.append("]").toString());
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error("GET failed: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // Read raw body first for debugging
        BufferedReader reader = req.getReader();
        StringBuilder rawBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) rawBody.append(line);
        String jsonBody = rawBody.toString().trim();

        System.out.println("StudentServlet POST body: " + jsonBody);

        try {
            // Parse each field safely
            String fname    = extractField(jsonBody, "fname");
            String mname    = extractField(jsonBody, "mname");
            String lname    = extractField(jsonBody, "lname");
            String email    = extractField(jsonBody, "email");
            String phone    = extractField(jsonBody, "phone");
            String gender   = extractField(jsonBody, "gender");
            String enroll   = extractField(jsonBody, "enrollmentNo");
            String program  = extractField(jsonBody, "program");
            String password = extractField(jsonBody, "password");
            String yearStr  = extractField(jsonBody, "currentYear");
            int currentYear = (yearStr != null && !yearStr.isEmpty()) ? Integer.parseInt(yearStr.trim()) : 1;

            // Validate required fields
            if (fname == null || fname.isEmpty())    { res.getWriter().write(JsonUtil.error("First name is required")); return; }
            if (lname == null || lname.isEmpty())    { res.getWriter().write(JsonUtil.error("Last name is required")); return; }
            if (email == null || email.isEmpty())    { res.getWriter().write(JsonUtil.error("Email is required")); return; }
            if (enroll == null || enroll.isEmpty())  { res.getWriter().write(JsonUtil.error("Enrollment number is required")); return; }
            if (program == null || program.isEmpty()){ res.getWriter().write(JsonUtil.error("Program is required")); return; }
            if (password == null || password.isEmpty()){ res.getWriter().write(JsonUtil.error("Password is required")); return; }

            Student s = new Student();
            s.setFname(fname);
            s.setMname(mname != null ? mname : "");
            s.setLname(lname);
            s.setEmail(email);
            s.setPhone(phone != null ? phone : "");
            s.setGender(gender != null ? gender : "");
            s.setEnrollmentNo(enroll);
            s.setProgram(program);
            s.setCurrentYear(currentYear);

            boolean ok = new StudentDAO().addStudent(s, password);
            if (ok) {
                res.getWriter().write(JsonUtil.ok("Student added successfully"));
            } else {
                res.getWriter().write(JsonUtil.error("Database insert returned false"));
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            // Duplicate email or enrollment number
            String msg = e.getMessage().contains("email")
                ? "A student with this email already exists"
                : "A student with this enrollment number already exists";
            res.getWriter().write(JsonUtil.error(msg));
        } catch (Exception e) {
            res.setStatus(500);
            String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            res.getWriter().write(JsonUtil.error("Failed to add student: " + errorMsg));
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        try {
            BufferedReader reader = req.getReader();
            StringBuilder rawBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) rawBody.append(line);
            String jsonBody = rawBody.toString().trim();

            String studentIdStr = extractField(jsonBody, "studentId");
            if (studentIdStr == null) { res.getWriter().write(JsonUtil.error("studentId missing")); return; }
            int studentId = Integer.parseInt(studentIdStr.trim());

            boolean ok = new StudentDAO().updateStudent(
                studentId,
                extractField(jsonBody, "fname"),
                extractField(jsonBody, "mname"),
                extractField(jsonBody, "lname"),
                extractField(jsonBody, "phone"),
                extractField(jsonBody, "gender"),
                extractField(jsonBody, "program"),
                Integer.parseInt(extractField(jsonBody, "currentYear").trim())
            );
            res.getWriter().write(ok ? JsonUtil.ok("Student updated") : JsonUtil.error("Update failed"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error("PUT failed: " + e.getMessage()));
            e.printStackTrace();
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
            boolean ok = new StudentDAO().deleteStudent(id);
            res.getWriter().write(ok ? JsonUtil.ok("Deleted") : JsonUtil.error("Student not found"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error("DELETE failed: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // Safe field extractor — handles missing fields gracefully
    private String extractField(String json, String key) {
        if (json == null || json.isEmpty()) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int afterKey = idx + search.length();
        // skip whitespace and colon
        while (afterKey < json.length() && (json.charAt(afterKey) == ' ' || json.charAt(afterKey) == ':')) afterKey++;
        if (afterKey >= json.length()) return null;
        char first = json.charAt(afterKey);
        if (first == '"') {
            // String value
            int start = afterKey + 1;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && (end == 0 || json.charAt(end - 1) != '\\')) break;
                end++;
            }
            return json.substring(start, end);
        } else if (first == 'n') {
            return null; // null value
        } else {
            // Number or boolean
            int start = afterKey;
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(start, end).trim();
        }
    }

    private String studentToJson(Student s) {
        return "{" +
            "\"studentId\":"    + s.getStudentId()   + "," +
            "\"personId\":"     + s.getPersonId()    + "," +
            "\"enrollmentNo\":\"" + j(s.getEnrollmentNo()) + "\"," +
            "\"program\":\""    + j(s.getProgram())  + "\"," +
            "\"currentYear\":"  + s.getCurrentYear() + "," +
            "\"yearOfAdmission\":" + s.getYearOfAdmission() + "," +
            "\"fname\":\""      + j(s.getFname())    + "\"," +
            "\"mname\":\""      + j(s.getMname())    + "\"," +
            "\"lname\":\""      + j(s.getLname())    + "\"," +
            "\"email\":\""      + j(s.getEmail())    + "\"," +
            "\"phone\":\""      + j(s.getPhone())    + "\"," +
            "\"gender\":\""     + j(s.getGender())   + "\"" +
            "}";
    }

    private String j(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
