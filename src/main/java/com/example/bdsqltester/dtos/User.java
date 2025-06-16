package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    public long id;
    public String username;
    public String password;
    public String role;

    // PERUBAHAN: Menyimpan NIP guru (String)
    private String nip;

    public User(ResultSet rs) throws SQLException {
        this.id = rs.getLong("id");
        this.username = rs.getString("username");
        this.password = rs.getString("password");
        this.role = rs.getString("role");
    }

    public String getUsername() {
        return username;
    }

    // --- Getter dan Setter BARU untuk NIP ---
    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }
}