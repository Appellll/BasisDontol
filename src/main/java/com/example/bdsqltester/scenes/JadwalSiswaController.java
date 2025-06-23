package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.JadwalSiswa;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JadwalSiswaController {

    @FXML private Label judulLabel;
    @FXML private TableView<JadwalSiswa> jadwalTable;
    @FXML private TableColumn<JadwalSiswa, String> hariColumn;
    @FXML private TableColumn<JadwalSiswa, String> jamColumn;
    @FXML private TableColumn<JadwalSiswa, String> mapelColumn;
    @FXML private Button backButton;

    private User user;
    private ObservableList<JadwalSiswa> jadwalList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        hariColumn.setCellValueFactory(cellData -> cellData.getValue().hariProperty());
        jamColumn.setCellValueFactory(cellData -> cellData.getValue().jamProperty());
        mapelColumn.setCellValueFactory(cellData -> cellData.getValue().mataPelajaranProperty());
    }

    public void setUser(User user) {
        this.user = user;
        // Tampilkan judul dengan nama kelas dari objek User
        if (user.getKelas() != null) {
            judulLabel.setText("Jadwal Kelas: " + user.getKelas());
        } else {
            judulLabel.setText("Jadwal Kelas");
        }
        loadJadwal();
    }

    private void loadJadwal() {
        String findKelasSql = "SELECT kelas_id FROM siswa WHERE nama_siswa = ?";

        // --- QUERY SQL SUDAH DIPERBAIKI ---
        String jadwalSql = "SELECT j.hari, j.jam_mulai, j.jam_selesai, m.nama_mapel " +
                "FROM jadwal j " +
                "JOIN mapel m ON j.mapel_id = m.mapel_id " +
                "WHERE j.kelas_id = ? " +
                "ORDER BY " +
                "  CASE j.hari " +
                "    WHEN 'Senin' THEN 1 " +
                "    WHEN 'Selasa' THEN 2 " +
                "    WHEN 'Rabu' THEN 3 " +
                "    WHEN 'Kamis' THEN 4 " +
                "    WHEN 'Jumat' THEN 5 " +
                "    ELSE 6 " +
                "  END, " +
                "  j.jam_mulai";

        try (Connection connection = MainDataSource.getConnection();
             PreparedStatement findKelasStmt = connection.prepareStatement(findKelasSql)) {

            findKelasStmt.setString(1, this.user.getUsername());
            ResultSet rsKelas = findKelasStmt.executeQuery();

            if (rsKelas.next()) {
                int kelasId = rsKelas.getInt("kelas_id");

                try (PreparedStatement jadwalStmt = connection.prepareStatement(jadwalSql)) {
                    jadwalStmt.setInt(1, kelasId);
                    ResultSet rsJadwal = jadwalStmt.executeQuery();

                    jadwalList.clear();
                    while (rsJadwal.next()) {
                        jadwalList.add(new JadwalSiswa(
                                rsJadwal.getString("hari"),
                                rsJadwal.getString("jam_mulai"),
                                rsJadwal.getString("jam_selesai"),
                                rsJadwal.getString("nama_mapel")
                        ));
                    }
                    jadwalTable.setItems(jadwalList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal mengambil data jadwal dari database.");
        }
    }

    @FXML
    void onBackClick(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
        Scene scene = new Scene(loader.load());
        SiswaController siswaController = loader.getController();
        siswaController.setUser(this.user);
        currentStage.setScene(scene);
        currentStage.setTitle("Menu Siswa");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}