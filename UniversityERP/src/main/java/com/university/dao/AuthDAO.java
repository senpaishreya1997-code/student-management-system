package com.university.dao;

import com.university.model.Person;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class AuthDAO {

    public Person authenticate(String email, String password) throws SQLException {
        String hash = sha256(password);
        String sql = "SELECT * FROM Person WHERE email = ? AND password_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, hash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Person p = new Person();
                p.setPersonId(rs.getInt("person_id"));
                p.setFname(rs.getString("fname"));
                p.setMname(rs.getString("mname"));
                p.setLname(rs.getString("lname"));
                p.setEmail(rs.getString("email"));
                p.setPhone(rs.getString("phone"));
                p.setRole(rs.getString("role"));
                return p;
            }
        }
        return null;
    }

    public boolean changePassword(int personId, String newPassword) throws SQLException {
        String sql = "UPDATE Person SET password_hash = ? WHERE person_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sha256(newPassword));
            ps.setInt(2, personId);
            return ps.executeUpdate() > 0;
        }
    }

    // PUBLIC so StudentDAO and InstructorDAO can call it
    public String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 failed", e);
        }
    }
}
