package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Jadwal {
    private String hari;
    private String jamMulai;
    private String jamSelesai;
    private String namaMapel;

    // Constructor untuk mengambil data dari ResultSet
    public Jadwal(ResultSet rs) throws SQLException {
        this.hari = rs.getString("hari");
        this.jamMulai = rs.getString("jam_mulai");
        this.jamSelesai = rs.getString("jam_selesai");
        this.namaMapel = rs.getString("nama_mapel");
    }

    // PENTING: Override metode toString()
    // Metode ini akan dipanggil oleh ListView untuk menampilkan setiap item.
    @Override
    public String toString() {
        // Contoh output: "Selasa: Matematika (7.30 - 8.30)"
        return String.format("%s: %s (%s - %s)", this.hari, this.namaMapel, this.jamMulai, this.jamSelesai);
    }

    // Getter jika diperlukan
    public String getHari() {
        return hari;
    }

    public String getNamaMapel() {
        return namaMapel;
    }
}
