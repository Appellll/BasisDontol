package com.example.bdsqltester.dtos;

import javafx.beans.property.SimpleStringProperty;

public class JadwalSiswa {
    private final SimpleStringProperty hari;
    private final SimpleStringProperty jam;
    private final SimpleStringProperty mapel;
    private final SimpleStringProperty guru;

    public JadwalSiswa(String hari, String jam, String mapel, String guru) {
        this.hari = new SimpleStringProperty(hari);
        this.jam = new SimpleStringProperty(jam);
        this.mapel = new SimpleStringProperty(mapel);
        this.guru = new SimpleStringProperty(guru);
    }

    public SimpleStringProperty hariProperty() { return hari; }
    public SimpleStringProperty jamProperty() { return jam; }
    public SimpleStringProperty mapelProperty() { return mapel; }
    public SimpleStringProperty guruProperty() { return guru; }
}