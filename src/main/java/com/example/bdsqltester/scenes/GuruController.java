package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuruController {
    @FXML private Label namaUser;
    private User user;
    private Connection connection;

    public GuruController() {
        try {
            this.connection = MainDataSource.getConnection();
        } catch (SQLException e) {
            showAlert("Fatal Error", "Tidak dapat terhubung ke database.");
            e.printStackTrace();
        }
    }

    // --- METODE INI DIMODIFIKASI ---
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            namaUser.setText("Halo, " + user.getUsername()+"! 👋");
            // AMBIL NIP DARI DATABASE SETELAH LOGIN BERHASIL
            loadGuruDetails();
        }
    }

    // --- METODE BARU UNTUK MENGAMBIL DETAIL GURU ---
    private void loadGuruDetails() {
        String sql = "SELECT nip FROM guru WHERE nama_guru = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, this.user.getUsername());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Simpan NIP ke dalam objek User
                this.user.setNip(rs.getString("nip"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal mengambil detail NIP guru.");
        }
    }

    @FXML
    public void initialize() {
        if (connection == null) {
            showAlert("Error", "Koneksi ke database GAGAL.");
        }
    }

    @FXML
    public void OnInputNilaiClick(ActionEvent actionEvent) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_nilai-view.fxml"));
            Scene scene = new Scene(loader.load());
            InputNilaiController inputNilaiController = loader.getController();
            inputNilaiController.setUser(this.user);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void OnJadwalMengajarClick(ActionEvent actionEvent) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-jadwal-view.fxml"));
            Scene scene = new Scene(loader.load());
            GuruJadwalController jadwalController = loader.getController();
            jadwalController.setUser(this.user);
            app.getPrimaryStage().setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman jadwal mengajar.");
        }
    }

    @FXML
    public void OnLogoutClick(ActionEvent actionEvent) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}