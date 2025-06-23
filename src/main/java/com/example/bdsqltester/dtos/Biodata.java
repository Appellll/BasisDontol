package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Biodata {
    public final String namaSiswa;
    public final String tanggalLahir;
    public final String jenisKelamin;
    public final String alamat;
    public final String agama;
    public final String telpOrangTua;
    public final String namaIbu;
    public final String namaAyah;

    public Biodata(ResultSet rs) throws SQLException {
        this.namaSiswa = rs.getString("nama_siswa");
        this.tanggalLahir = rs.getString("tanggal_lahir");
        this.jenisKelamin = rs.getString("jenis_kelamin");
        this.alamat = rs.getString("alamat");
        this.agama = rs.getString("agama");
        this.telpOrangTua = rs.getString("telp_orang_tua");
        this.namaIbu = rs.getString("Ibu");
        this.namaAyah = rs.getString("Ayah");
    }
}