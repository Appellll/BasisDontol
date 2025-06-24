package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RaporSiswaController implements SiswaDataReceiver {

    @FXML private TableView<RaporEntry> raporTable;
    @FXML private TableColumn<RaporEntry, String> mapelColumn;
    @FXML private TableColumn<RaporEntry, Integer> nilaiColumn;
    @FXML private TableColumn<RaporEntry, Integer> kkmColumn;
    @FXML private TableColumn<RaporEntry, String> statusColumn;
    @FXML private Label headerLabel;

    private User currentUser;
    private final ObservableList<RaporEntry> raporDataList = FXCollections.observableArrayList();

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        headerLabel.setText("Rapor Akademik: " + user.getUsername());
        loadRaporData();
    }

    @FXML
    public void initialize() {
        raporTable.setItems(raporDataList);
        mapelColumn.setCellValueFactory(cellData -> cellData.getValue().namaMapelProperty());
        nilaiColumn.setCellValueFactory(cellData -> cellData.getValue().nilaiProperty().asObject());
        kkmColumn.setCellValueFactory(cellData -> cellData.getValue().kkmProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadRaporData() {
        raporDataList.clear();
        String sql = "SELECT m.nama_mapel, n.nilai_siswa, m.kkm_mapel " +
                "FROM nilai n " +
                "JOIN mapel m ON n.mapel_id = m.mapel_id " +
                "WHERE n.siswa_id = ?";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUser.getId()); // NIS siswa
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                String namaMapel = rs.getString("nama_mapel");
                int nilai = rs.getInt("nilai_siswa");
                int kkm = rs.getInt("kkm_mapel");
                String status = (nilai >= kkm) ? "Tuntas" : "Belum Tuntas";

                raporDataList.add(new RaporEntry(namaMapel, nilai, kkm, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data rapor.");
        }
    }

    @FXML
    void onBackClick(ActionEvent event) {
        SiswaControllerUtil.backToSiswaDashboard(currentUser);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class RaporEntry {
        private final SimpleStringProperty namaMapel;
        private final SimpleIntegerProperty nilai;
        private final SimpleIntegerProperty kkm;
        private final SimpleStringProperty status;

        public RaporEntry(String namaMapel, int nilai, int kkm, String status) {
            this.namaMapel = new SimpleStringProperty(namaMapel);
            this.nilai = new SimpleIntegerProperty(nilai);
            this.kkm = new SimpleIntegerProperty(kkm);
            this.status = new SimpleStringProperty(status);
        }

        public SimpleStringProperty namaMapelProperty() { return namaMapel; }
        public SimpleIntegerProperty nilaiProperty() { return nilai; }
        public SimpleIntegerProperty kkmProperty() { return kkm; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}