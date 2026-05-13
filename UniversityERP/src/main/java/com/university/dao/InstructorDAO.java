package com.university.dao;

import java.sql.*;
import java.util.*;

public class InstructorDAO {

    public Map<String,Object> getByPersonId(int personId) throws SQLException {
        String sql = """
            SELECT i.instructor_id, i.experience_years, i.title,
                   p.person_id, p.fname, p.lname, p.email, p.phone,
                   d.dept_name, d.dept_id, e.employee_id, e.salary, e.join_date
            FROM Instructor i
            JOIN Employee e ON i.employee_id = e.employee_id
            JOIN Person p ON e.person_id = p.person_id
            LEFT JOIN Department d ON e.dept_id = d.dept_id
            WHERE e.person_id = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public Map<String,Object> getByInstructorId(int instructorId) throws SQLException {
        String sql = """
            SELECT i.instructor_id, i.experience_years, i.title,
                   p.person_id, p.fname, p.lname, p.email, p.phone,
                   d.dept_name, d.dept_id, e.employee_id, e.salary, e.join_date
            FROM Instructor i
            JOIN Employee e ON i.employee_id = e.employee_id
            JOIN Person p ON e.person_id = p.person_id
            LEFT JOIN Department d ON e.dept_id = d.dept_id
            WHERE i.instructor_id = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Map<String,Object>> getAll() throws SQLException {
        List<Map<String,Object>> list = new ArrayList<>();
        String sql = """
            SELECT i.instructor_id, i.experience_years, i.title,
                   p.person_id, p.fname, p.lname, p.email, p.phone,
                   d.dept_name, d.dept_id, e.employee_id, e.salary, e.join_date
            FROM Instructor i
            JOIN Employee e ON i.employee_id = e.employee_id
            JOIN Person p ON e.person_id = p.person_id
            LEFT JOIN Department d ON e.dept_id = d.dept_id
            ORDER BY p.lname
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public boolean addInstructor(String fname, String mname, String lname, String email,
                                  String phone, String title, int deptId, int experience,
                                  double salary, String joinDate, String password) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String hash = new AuthDAO().sha256(password);
            String pSql = "INSERT INTO Person (fname,mname,lname,email,phone,password_hash,role) VALUES (?,?,?,?,?,?,'instructor')";
            PreparedStatement ps = conn.prepareStatement(pSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fname); ps.setString(2, mname); ps.setString(3, lname);
            ps.setString(4, email); ps.setString(5, phone); ps.setString(6, hash);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int personId = rs.next() ? rs.getInt(1) : 0;

            String eSql = "INSERT INTO Employee (person_id,join_date,salary,dept_id) VALUES (?,?,?,?)";
            PreparedStatement ps2 = conn.prepareStatement(eSql, Statement.RETURN_GENERATED_KEYS);
            ps2.setInt(1, personId); ps2.setString(2, joinDate);
            ps2.setDouble(3, salary); ps2.setInt(4, deptId);
            ps2.executeUpdate();
            ResultSet rs2 = ps2.getGeneratedKeys();
            int empId = rs2.next() ? rs2.getInt(1) : 0;

            String iSql = "INSERT INTO Instructor (employee_id, experience_years, title) VALUES (?,?,?)";
            PreparedStatement ps3 = conn.prepareStatement(iSql);
            ps3.setInt(1, empId); ps3.setInt(2, experience); ps3.setString(3, title);
            ps3.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally {
            conn.setAutoCommit(true); conn.close();
        }
    }

    public boolean updateInstructor(int instructorId, String fname, String lname,
                                     String phone, String title, int deptId, int experience) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // Update Person
            String pSql = """
                UPDATE Person SET fname=?, lname=?, phone=?
                WHERE person_id=(
                    SELECT ep.person_id FROM Employee ep
                    JOIN Instructor ii ON ii.employee_id=ep.employee_id
                    WHERE ii.instructor_id=?
                )
            """;
            PreparedStatement ps = conn.prepareStatement(pSql);
            ps.setString(1, fname); ps.setString(2, lname);
            ps.setString(3, phone); ps.setInt(4, instructorId);
            ps.executeUpdate();

            // Update Employee dept
            String eSql = """
                UPDATE Employee SET dept_id=?
                WHERE employee_id=(SELECT employee_id FROM Instructor WHERE instructor_id=?)
            """;
            PreparedStatement ps2 = conn.prepareStatement(eSql);
            ps2.setInt(1, deptId); ps2.setInt(2, instructorId);
            ps2.executeUpdate();

            // Update Instructor
            String iSql = "UPDATE Instructor SET title=?, experience_years=? WHERE instructor_id=?";
            PreparedStatement ps3 = conn.prepareStatement(iSql);
            ps3.setString(1, title); ps3.setInt(2, experience); ps3.setInt(3, instructorId);
            ps3.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally {
            conn.setAutoCommit(true); conn.close();
        }
    }

    public boolean deleteInstructor(int instructorId) throws SQLException {
        String sql = """
            DELETE FROM Person WHERE person_id=(
                SELECT ep.person_id FROM Employee ep
                JOIN Instructor i ON i.employee_id=ep.employee_id
                WHERE i.instructor_id=?
            )
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            return ps.executeUpdate() > 0;
        }
    }

    private Map<String,Object> mapRow(ResultSet rs) throws SQLException {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("instructorId", rs.getInt("instructor_id"));
        m.put("fname",        rs.getString("fname"));
        m.put("lname",        rs.getString("lname"));
        m.put("email",        rs.getString("email"));
        m.put("phone",        rs.getString("phone") != null ? rs.getString("phone") : "");
        m.put("title",        rs.getString("title") != null ? rs.getString("title") : "");
        m.put("deptName",     rs.getString("dept_name") != null ? rs.getString("dept_name") : "");
        m.put("deptId",       rs.getInt("dept_id"));
        m.put("experienceYears", rs.getInt("experience_years"));
        m.put("employeeId",   rs.getInt("employee_id"));
        m.put("salary",       rs.getDouble("salary"));
        m.put("joinDate",     rs.getString("join_date") != null ? rs.getString("join_date") : "");
        return m;
    }
}
