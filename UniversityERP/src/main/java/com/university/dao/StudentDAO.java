package com.university.dao;

import com.university.model.Student;
import java.sql.*;
import java.util.*;

public class StudentDAO {

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = """
            SELECT s.student_id, s.enrollment_no, s.program, s.current_year, s.year_of_admission,
                   p.person_id, p.fname, p.mname, p.lname, p.email, p.phone, p.gender
            FROM Student s JOIN Person p ON s.person_id = p.person_id
            ORDER BY s.enrollment_no
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapStudent(rs));
        }
        return list;
    }

    public Student getByPersonId(int personId) throws SQLException {
        String sql = """
            SELECT s.student_id, s.enrollment_no, s.program, s.current_year, s.year_of_admission,
                   p.person_id, p.fname, p.mname, p.lname, p.email, p.phone, p.gender
            FROM Student s JOIN Person p ON s.person_id = p.person_id
            WHERE s.person_id = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapStudent(rs);
        }
        return null;
    }

    public Student getById(int studentId) throws SQLException {
        String sql = """
            SELECT s.student_id, s.enrollment_no, s.program, s.current_year, s.year_of_admission,
                   p.person_id, p.fname, p.mname, p.lname, p.email, p.phone, p.gender
            FROM Student s JOIN Person p ON s.person_id = p.person_id
            WHERE s.student_id = ?
        """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapStudent(rs);
        }
        return null;
    }

    public boolean addStudent(Student s, String password) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String hash = new AuthDAO().sha256(password);
            String pSql = "INSERT INTO Person (fname,mname,lname,email,phone,gender,password_hash,role) VALUES (?,?,?,?,?,?,?,'student')";
            PreparedStatement ps = conn.prepareStatement(pSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getFname());
            ps.setString(2, s.getMname());
            ps.setString(3, s.getLname());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getPhone());
            ps.setString(6, s.getGender());
            ps.setString(7, hash);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int personId = rs.next() ? rs.getInt(1) : 0;
            if (personId == 0) throw new SQLException("Failed to create person");

            String sSql = "INSERT INTO Student (person_id,enrollment_no,program,year_of_admission,current_year) VALUES (?,?,?,YEAR(NOW()),?)";
            PreparedStatement ps2 = conn.prepareStatement(sSql);
            ps2.setInt(1, personId);
            ps2.setString(2, s.getEnrollmentNo());
            ps2.setString(3, s.getProgram());
            ps2.setInt(4, s.getCurrentYear());
            ps2.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public boolean updateStudent(int studentId, String fname, String mname, String lname,
                                  String phone, String gender, String program, int currentYear) throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            String pSql = """
                UPDATE Person SET fname=?, mname=?, lname=?, phone=?, gender=?
                WHERE person_id=(SELECT person_id FROM Student WHERE student_id=?)
            """;
            PreparedStatement ps = conn.prepareStatement(pSql);
            ps.setString(1, fname); ps.setString(2, mname); ps.setString(3, lname);
            ps.setString(4, phone); ps.setString(5, gender); ps.setInt(6, studentId);
            ps.executeUpdate();

            String sSql = "UPDATE Student SET program=?, current_year=? WHERE student_id=?";
            PreparedStatement ps2 = conn.prepareStatement(sSql);
            ps2.setString(1, program); ps2.setInt(2, currentYear); ps2.setInt(3, studentId);
            ps2.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback(); throw e;
        } finally {
            conn.setAutoCommit(true); conn.close();
        }
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM Person WHERE person_id=(SELECT person_id FROM Student WHERE student_id=?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setEnrollmentNo(rs.getString("enrollment_no"));
        s.setProgram(rs.getString("program"));
        s.setCurrentYear(rs.getInt("current_year"));
        s.setYearOfAdmission(rs.getInt("year_of_admission"));
        s.setPersonId(rs.getInt("person_id"));
        s.setFname(rs.getString("fname"));
        s.setMname(rs.getString("mname"));
        s.setLname(rs.getString("lname"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setGender(rs.getString("gender"));
        return s;
    }
}
