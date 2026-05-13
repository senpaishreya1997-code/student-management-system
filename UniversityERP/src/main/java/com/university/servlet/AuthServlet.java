package com.university.servlet;

import com.university.dao.AuthDAO;
import com.university.dao.JsonUtil;
import com.university.model.Person;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        try {
            Person person = new AuthDAO().authenticate(email, password);
            if (person != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("person", person);
                session.setAttribute("role", person.getRole());
                session.setMaxInactiveInterval(60 * 30);

                String redirect;
                switch (person.getRole()) {
                    case "student"    -> redirect = "student/dashboard.html";
                    case "instructor" -> redirect = "faculty/dashboard.html";
                    default           -> redirect = "admin/dashboard.html";
                }
                res.getWriter().write("{\"success\":true,\"role\":\"" + person.getRole()
                    + "\",\"name\":\"" + person.getFullName()
                    + "\",\"redirect\":\"" + redirect + "\"}");
            } else {
                res.setStatus(401);
                res.getWriter().write(JsonUtil.error("Invalid email or password"));
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write(JsonUtil.error(e.getMessage()));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        res.sendRedirect(req.getContextPath() + "/index.html");
    }
}
