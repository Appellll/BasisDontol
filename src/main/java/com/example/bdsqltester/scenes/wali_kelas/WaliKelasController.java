package com.example.bdsqltester.scenes.wali_kelas;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import java.io.IOException;

public class WaliKelasController {

    @FXML
    private Label namaWaliKelasLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            namaWaliKelasLabel.setText("Selamat Datang, " + user.getUsername() + "!");
        }
    }

    @FXML
    void onAbsenSiswaClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("absen_siswa-view.fxml"));
            Scene scene = new Scene(loader.load());

            AbsenSiswaController controller = loader.getController();
            controller.setUser(this.user);

            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Absensi Siswa");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman absensi siswa.");
            e.printStackTrace();
        }
    }

    @FXML
    void onPrintRaporClick(ActionEvent event) { // <-- PERUBAHAN NAMA METODE DI SINI
        // Logika untuk cetak rapor bisa ditambahkan di sini
        // Untuk saat ini, kita akan menavigasi ke halaman print rapor
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("print_rapor-view.fxml"));
            Scene scene = new Scene(loader.load());
            PrintRaporController controller = loader.getController();
            controller.setUser(this.user); // Mengirim data user
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Cetak Rapor");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman cetak rapor.");
            e.printStackTrace();
        }
    }

    @FXML
    void onLogoutClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(new Scene(loader.load()));
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Login");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal kembali ke halaman login.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}