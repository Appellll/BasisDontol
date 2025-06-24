package com.example.bdsqltester.dtos;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class NilaiSiswa {
    private final SimpleStringProperty mapel;
    private final SimpleStringProperty tipe;
    private final SimpleIntegerProperty nilai;

    public NilaiSiswa(String mapel, String tipe, int nilai) {
        this.mapel = new SimpleStringProperty(mapel);
        this.tipe = new SimpleStringProperty(tipe);
        this.nilai = new SimpleIntegerProperty(nilai);
    }

    public SimpleStringProperty mapelProperty() { return mapel; }
    public SimpleStringProperty tipeProperty() { return tipe; }
    public SimpleIntegerProperty nilaiProperty() { return nilai; }
}