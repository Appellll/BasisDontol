package com.example.bdsqltester.dtos;

public class User {
    // Ubah id menjadi String untuk mengakomodasi NIP/NIS
    public String id;
    public String username;
    public String role;
    private String nip;
    private boolean isWaliKelas = false;

    // Konstruktor kosong
    public User() {
    }

    // --- Getters dan Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public boolean isWaliKelas() {
        return isWaliKelas;
    }

    public void setWaliKelas(boolean waliKelas) {
        isWaliKelas = waliKelas;
    }
}