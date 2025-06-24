package com.example.bdsqltester.dtos;

import javafx.beans.property.SimpleStringProperty;

public class SppSiswa {
    private final SimpleStringProperty bulan;
    private final SimpleStringProperty tagihan;
    private final SimpleStringProperty status;

    public SppSiswa(String bulan, String tagihan, String status) {
        this.bulan = new SimpleStringProperty(bulan);
        this.tagihan = new SimpleStringProperty(tagihan);
        this.status = new SimpleStringProperty(status);
    }

    public SimpleStringProperty bulanProperty() { return bulan; }
    public SimpleStringProperty tagihanProperty() { return tagihan; }
    public SimpleStringProperty statusProperty() { return status; }
}