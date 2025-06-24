package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;

public class AdminController {

    @FXML
    private Label namaUser;

    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            namaUser.setText("Halo, " + user.getUsername() + "! 👋");
        }
    }

    @FXML
    void onLogoutClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Login");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman login.");
        }
    }

    @FXML
    void onInputSiswaClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_siswa-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Input Manajemen Siswa");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman input siswa.");
        }
    }

    @FXML
    void onInputJadwalClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_jadwal-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Manajemen Jadwal");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman jadwal.");
        }
    }

    @FXML
    void onInputBiodataClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_biodata_siswa-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Input Biodata Siswa");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman input biodata siswa.");
        }
    }

    @FXML
    void onInputSppClick(ActionEvent event) {
        // --- PERUBAHAN DI SINI ---
        // Mengganti alert dengan logika untuk memuat scene SPP
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_spp-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Input SPP");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman input SPP.");
        }
    }

    @FXML
    void onInputKelasClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_kelas-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Input Manajemen Kelas");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman input kelas.");
        }
    }

    @FXML
    void onInputMapelClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_mapel-view.fxml"));
            Scene scene = new Scene(loader.load());
            app.getPrimaryStage().setTitle("Input Mapel");
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Gagal memuat halaman input mapel.");
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