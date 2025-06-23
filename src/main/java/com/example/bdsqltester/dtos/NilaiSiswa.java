package com.example.bdsqltester.dtos;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NilaiSiswa {
    private final StringProperty mataPelajaran;
    private final StringProperty tipeNilai;
    private final IntegerProperty nilai;
    private final StringProperty tanggal;
    private final StringProperty namaGuru; // <-- TAMBAHAN BARU

    public NilaiSiswa(ResultSet rs) throws SQLException {
        this.mataPelajaran = new SimpleStringProperty(rs.getString("nama_mapel"));
        this.tipeNilai = new SimpleStringProperty(rs.getString("tipe_nilai"));
        this.nilai = new SimpleIntegerProperty(rs.getInt("nilai_siswa"));
        this.namaGuru = new SimpleStringProperty(rs.getString("nama_guru")); // <-- TAMBAHAN BARU

        // Format tanggal agar lebih mudah dibaca
        LocalDate date = rs.getDate("tanggal_input").toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        this.tanggal = new SimpleStringProperty(date.format(formatter));
    }

    // Getter untuk setiap property
    public StringProperty mataPelajaranProperty() { return mataPelajaran; }
    public StringProperty tipeNilaiProperty() { return tipeNilai; }
    public IntegerProperty nilaiProperty() { return nilai; }
    public StringProperty tanggalProperty() { return tanggal; }
    public StringProperty namaGuruProperty() { return namaGuru; } // <-- TAMBAHAN BARU
}