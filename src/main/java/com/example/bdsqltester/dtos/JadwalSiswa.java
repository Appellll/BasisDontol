package com.example.bdsqltester.dtos;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class JadwalSiswa {
    private final StringProperty hari;
    private final StringProperty jam;
    private final StringProperty mataPelajaran;

    public JadwalSiswa(String hari, String jamMulai, String jamSelesai, String mataPelajaran) {
        this.hari = new SimpleStringProperty(hari);
        this.jam = new SimpleStringProperty(jamMulai + " - " + jamSelesai);
        this.mataPelajaran = new SimpleStringProperty(mataPelajaran);
    }

    // Getter untuk setiap property (diperlukan oleh TableView)
    public StringProperty hariProperty() {
        return hari;
    }

    public StringProperty jamProperty() {
        return jam;
    }

    public StringProperty mataPelajaranProperty() {
        return mataPelajaran;
    }
}