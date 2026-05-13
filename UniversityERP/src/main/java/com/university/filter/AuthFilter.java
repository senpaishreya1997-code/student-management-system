package com.university.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;

@WebFilter(urlPatterns = {"/api/*", "/student/*", "/faculty/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        boolean loggedIn = session != null && session.getAttribute("person") != null;

        if (!loggedIn) {
            String requestedWith = req.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(requestedWith) || req.getRequestURI().contains("/api/")) {
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"error\":\"Not authenticated\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/index.html");
            }
            return;
        }

        // Role-based path guard
        String role = (String) session.getAttribute("role");
        String uri = req.getRequestURI();
        if (uri.contains("/student/") && !"student".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/index.html"); return;
        }
        if (uri.contains("/faculty/") && !"instructor".equals(role)) {
            res.sendRedirect(req.getContextPath() + "/index.html"); return;
        }
        if (uri.contains("/admin/") && (!"admin_staff".equals(role) && !"registrar".equals(role))) {
            res.sendRedirect(req.getContextPath() + "/index.html"); return;
        }

        chain.doFilter(request, response);
    }
}
