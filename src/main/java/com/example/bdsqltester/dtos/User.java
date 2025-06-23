package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    public long id;
    public String username;
    public String password;
    public String role;
    private String nip;

    // --- TAMBAHAN BARU UNTUK DATA SISWA ---
    private String kelas;
    private String tahunAjaran;
    // ------------------------------------

    public User(ResultSet rs) throws SQLException {
        this.id = rs.getLong("id");
        this.username = rs.getString("username");
        this.password = rs.getString("password");
        this.role = rs.getString("role");
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return this.id;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    // --- METODE BARU UNTUK KELAS DAN TAHUN AJARAN ---
    public String getKelas() {
        return kelas;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public String getTahunAjaran() {
        return tahunAjaran;
    }

    public void setTahunAjaran(String tahunAjaran) {
        this.tahunAjaran = tahunAjaran;
    }
    // ---------------------------------------------
}