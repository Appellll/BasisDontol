package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SiswaController {
    @FXML private Label namaSiswaLabel;
    @FXML private Label kelasLabel;
    private User user;
    private Connection connection;

    public SiswaController() {
        try {
            this.connection = MainDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            namaSiswaLabel.setText("Halo, " + user.getUsername() + "! 👋");
            if (user.getKelas() != null && user.getTahunAjaran() != null) {
                kelasLabel.setText("Kelas: " + user.getKelas() + " (" + user.getTahunAjaran() + ")");
            } else {
                loadSiswaDetails();
            }
        }
    }

    private void loadSiswaDetails() {
        String sql = "SELECT s.tahun_ajaran, k.nama_kelas " +
                "FROM siswa s " +
                "JOIN kelas k ON s.kelas_id = k.kelas_id " +
                "WHERE s.nama_siswa = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, this.user.getUsername());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.user.setKelas(rs.getString("nama_kelas"));
                this.user.setTahunAjaran(rs.getString("tahun_ajaran"));
                kelasLabel.setText("Kelas: " + user.getKelas() + " (" + user.getTahunAjaran() + ")");
            } else {
                kelasLabel.setText("Data kelas tidak ditemukan.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            kelasLabel.setText("Error memuat data kelas.");
        }
    }

    // --- METODE-METODE TOMBOL ---
    @FXML
    private void onBiodataClick(ActionEvent event) throws IOException {
        loadScene("biodata-view.fxml", "Biodata Siswa", event);
    }

    @FXML
    private void onJadwalClick(ActionEvent event) throws IOException {
        loadScene("jadwal-siswa-view.fxml", "Jadwal Kelas", event);
    }

    @FXML
    private void onNilaiClick(ActionEvent event) throws IOException {
        // --- INI BAGIAN YANG DIPERBARUI ---
        loadScene("nilai-siswa-view.fxml", "Nilai Ujian Siswa", event);
    }

    @FXML
    private void onLogoutClick(ActionEvent event) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Stage stage = (Stage) namaSiswaLabel.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Login");
    }

    // --- METODE HELPER YANG DISEMPURNAKAN ---
    private void loadScene(String fxmlFile, String title, ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load());

        // Cek tipe controller dan kirim data user
        Object controller = loader.getController();
        if (controller instanceof BiodataController) {
            ((BiodataController) controller).setUser(this.user);
        } else if (controller instanceof JadwalSiswaController) {
            ((JadwalSiswaController) controller).setUser(this.user);
        } else if (controller instanceof NilaiSiswaController) { // <-- PENAMBAHAN LOGIKA
            ((NilaiSiswaController) controller).setUser(this.user);
        }

        // Mengambil stage dari tombol yang ditekan
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}