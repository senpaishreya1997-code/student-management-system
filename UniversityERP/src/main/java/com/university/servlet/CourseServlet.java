package com.university.servlet;

import com.university.dao.CourseDAO;
import java.sql.SQLIntegrityConstraintViolationException;
import com.university.dao.InstructorDAO;
import com.university.dao.JsonUtil;
import com.university.model.Course;
import com.university.model.Person;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

@WebServlet("/api/courses/*")
public class CourseServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	        throws ServletException, IOException {
	    res.setContentType("application/json");
	    res.setCharacterEncoding("UTF-8");
	    String path = req.getPathInfo();

	    System.out.println("CourseServlet GET path: " + path);

	    try {
	        CourseDAO dao = new CourseDAO();

	        if (path == null || path.equals("/")) {
	            // GET /api/courses — all courses
	            List<Course> courses = dao.getAllCourses();
	            StringBuilder sb = new StringBuilder("[");
	            for (int i = 0; i < courses.size(); i++) {
	                if (i > 0) sb.append(",");
	                Course c = courses.get(i);
	                sb.append("{")
	                  .append("\"courseId\":").append(c.getCourseId()).append(",")
	                  .append("\"courseNo\":\"").append(j(c.getCourseNo())).append("\",")
	                  .append("\"title\":\"").append(j(c.getTitle())).append("\",")
	                  .append("\"credits\":").append(c.getCredits()).append(",")
	                  .append("\"syllabus\":\"").append(j(c.getSyllabus())).append("\",")
	                  .append("\"deptId\":").append(c.getDeptId()).append(",")
	                  .append("\"deptName\":\"").append(j(c.getDeptName())).append("\"")
	                  .append("}");
	            }
	            res.getWriter().write(sb.append("]").toString());

	        } else if (path.equals("/offerings")) {
	            // GET /api/courses/offerings — all offerings (admin)
	            res.getWriter().write(JsonUtil.toJson(dao.getAllOfferings()));

	        } else if (path.equals("/departments")) {
	            // GET /api/courses/departments
	            res.getWriter().write(JsonUtil.toJson(dao.getDepartments()));

	        } else if (path.equals("/instructors")) {
	            // GET /api/courses/instructors
	            res.getWriter().write(JsonUtil.toJson(dao.getInstructors()));

	        } else if (path.equals("/offerings/instructor/0")) {
	            // GET /api/courses/offerings/instructor/0
	            // Special: get offerings for the currently logged-in instructor
	            Person person = (Person) req.getSession().getAttribute("person");
	            if (person == null) {
	                res.setStatus(401);
	                res.getWriter().write(JsonUtil.error("Not authenticated"));
	                return;
	            }
	            System.out.println("Getting offerings for person_id: " + person.getPersonId());
	            InstructorDAO iDao = new InstructorDAO();
	            Map<String, Object> inst = iDao.getByPersonId(person.getPersonId());
	            System.out.println("Instructor found: " + inst);
	            if (inst != null) {
	                int iid = (int) inst.get("instructorId");
	                List<Map<String,Object>> offerings = dao.getOfferingsByInstructor(iid);
	                System.out.println("Offerings count: " + offerings.size());
	                res.getWriter().write(JsonUtil.toJson(offerings));
	            } else {
	                System.out.println("No instructor record found for person_id: " + person.getPersonId());
	                res.getWriter().write("[]");
	            }

	        } else if (path.startsWith("/offerings/instructor/")) {
	            // GET /api/courses/offerings/instructor/{id}
	            int instructorId = Integer.parseInt(
	                path.substring("/offerings/instructor/".length()).trim()
	            );
	            res.getWriter().write(JsonUtil.toJson(dao.getOfferingsByInstructor(instructorId)));

	        } else if (path.startsWith("/available/")) {
	            // GET /api/courses/available/{studentId}
	            int studentId = Integer.parseInt(
	                path.substring("/available/".length()).trim()
	            );
	            res.getWriter().write(JsonUtil.toJson(dao.getAvailableOfferingsForStudent(studentId)));

	        } else {
	            res.setStatus(400);
	            res.getWriter().write(JsonUtil.error("Unknown path: " + path));
	        }

	    } catch (Exception e) {
	        res.setStatus(500);
	        res.getWriter().write(JsonUtil.error("Error: " + e.getMessage()));
	        e.printStackTrace();
	    }
	}

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        // Read raw body
        BufferedReader reader = req.getReader();
        StringBuilder rawBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) rawBody.append(line);
        String jsonBody = rawBody.toString().trim();

        System.out.println("CourseServlet POST path=" + path + " body=" + jsonBody);

        try {
            CourseDAO dao = new CourseDAO();

            if ("/offerings".equals(path)) {
                String courseIdStr    = extractField(jsonBody, "courseId");
                String instIdStr      = extractField(jsonBody, "instructorId");
                String yearStr        = extractField(jsonBody, "year");
                String semester       = extractField(jsonBody, "semester");
                String sectionNo      = extractField(jsonBody, "sectionNo");
                String classroom      = extractField(jsonBody, "classroom");
                String timings        = extractField(jsonBody, "timings");
                String maxStr         = extractField(jsonBody, "maxStudents");

                if (courseIdStr == null || instIdStr == null || semester == null || sectionNo == null) {
                    res.getWriter().write(JsonUtil.error("Missing required fields"));
                    return;
                }

                boolean ok = dao.addOffering(
                    Integer.parseInt(courseIdStr.trim()),
                    Integer.parseInt(instIdStr.trim()),
                    Integer.parseInt(yearStr != null ? yearStr.trim() : "2024"),
                    semester, sectionNo,
                    classroom != null ? classroom : "",
                    timings != null ? timings : "",
                    maxStr != null && !maxStr.isEmpty() ? Integer.parseInt(maxStr.trim()) : 60
                );
                res.getWriter().write(ok ? JsonUtil.ok("Offering added") : JsonUtil.error("Failed to add offering"));

            } else {
                // Add course
                String courseNo = extractField(jsonBody, "courseNo");
                String title    = extractField(jsonBody, "title");
                String credits  = extractField(jsonBody, "credits");
                String syllabus = extractField(jsonBody, "syllabus");
                String deptId   = extractField(jsonBody, "deptId");

                if (courseNo == null || title == null || credits == null) {
                    res.getWriter().write(JsonUtil.error("Course number, title and credits are required"));
                    return;
                }

                Course c = new Course();
                c.setCourseNo(courseNo);
                c.setTitle(title);
                c.setCredits(Integer.parseInt(credits.trim()));
                c.setSyllabus(syllabus != null ? syllabus : "");
                c.setDeptId(deptId != null && !deptId.isEmpty() ? Integer.parseInt(deptId.trim()) : 1);

                boolean ok = dao.addCourse(c);
                res.getWriter().write(ok ? JsonUtil.ok("Course added") : JsonUtil.error("Failed to add course"));
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            res.getWriter().write(JsonUtil.error("Duplicate entry — course number or offering already exists"));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error("Failed: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    private String extractField(String json, String key) {
        if (json == null || json.isEmpty()) return null;
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int afterKey = idx + search.length();
        while (afterKey < json.length() && (json.charAt(afterKey) == ' ' || json.charAt(afterKey) == ':')) afterKey++;
        if (afterKey >= json.length()) return null;
        char first = json.charAt(afterKey);
        if (first == '"') {
            int start = afterKey + 1;
            int end = start;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && (end == 0 || json.charAt(end - 1) != '\\')) break;
                end++;
            }
            return json.substring(start, end);
        } else if (first == 'n') {
            return null;
        } else {
            int start = afterKey;
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(start, end).trim();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        try {
            CourseDAO dao = new CourseDAO();

            if (path != null && path.startsWith("/offerings/")) {
                // DELETE /api/courses/offerings/{id} — delete an offering
                int id = Integer.parseInt(path.substring("/offerings/".length()));
                boolean ok = dao.deleteOffering(id);
                res.getWriter().write(ok ? JsonUtil.ok("Offering deleted") : JsonUtil.error("Failed to delete"));

            } else if (path != null && path.length() > 1) {
                // DELETE /api/courses/{id} — delete a course
                int id = Integer.parseInt(path.substring(1));
                boolean ok = dao.deleteCourse(id);
                res.getWriter().write(ok ? JsonUtil.ok("Course deleted") : JsonUtil.error("Failed to delete"));

            } else {
                res.setStatus(400);
                res.getWriter().write(JsonUtil.error("Invalid request path"));
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
            CourseDAO dao = new CourseDAO();
            if (path != null && path.startsWith("/offerings/")) {
                int id = Integer.parseInt(path.substring("/offerings/".length()));
                boolean ok = dao.updateOffering(id,
                    Integer.parseInt(body.get("courseId").toString()),
                    Integer.parseInt(body.get("instructorId").toString()),
                    Integer.parseInt(body.get("year").toString()),
                    (String) body.get("semester"),
                    (String) body.get("sectionNo"),
                    (String) body.getOrDefault("classroom", ""),
                    (String) body.getOrDefault("timings", ""),
                    Integer.parseInt(body.getOrDefault("maxStudents", "60").toString())
                );
                res.getWriter().write(ok ? JsonUtil.ok("Offering updated") : JsonUtil.error("Failed"));
            } else if (path != null && path.length() > 1) {
                int id = Integer.parseInt(path.substring(1));
                boolean ok = dao.updateCourse(id,
                    (String) body.get("courseNo"),
                    (String) body.get("title"),
                    Integer.parseInt(body.get("credits").toString()),
                    (String) body.getOrDefault("syllabus", ""),
                    Integer.parseInt(body.getOrDefault("deptId", "1").toString())
                );
                res.getWriter().write(ok ? JsonUtil.ok("Course updated") : JsonUtil.error("Failed"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    // Safely escape a string value for JSON output
    private String j(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
