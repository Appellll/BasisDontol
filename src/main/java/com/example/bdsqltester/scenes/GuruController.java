package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Jadwal;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuruController {
    @FXML private ListView<Jadwal> listJadwal;
    @FXML private Label namaUser;

    private Connection connection;
    private User user;

    public GuruController() {
        try {
            this.connection = MainDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Fatal Error", "Tidak dapat terhubung ke database.");
        }
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            namaUser.setText("Halo, " + user.getUsername()+"! 👋");
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
        // --- TAMBAHKAN BARIS INI UNTUK TES ---
        System.out.println("DEBUG: Tombol 'Input Nilai' DIKLIK! Memulai proses...");
        // ------------------------------------

        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_nilai-view.fxml"));
            Scene scene = new Scene(loader.load());

            // Get the controller and pass the user data
            InputNilaiController inputNilaiController = loader.getController();
            inputNilaiController.setUser(this.user); // Pass the logged-in user

            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            // Jika ada error saat memuat FXML, cetak di sini
            System.out.println("ERROR: Gagal memuat input_nilai-view.fxml");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void OnLogoutClick(ActionEvent actionEvent) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Login");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal memuat halaman login.");
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