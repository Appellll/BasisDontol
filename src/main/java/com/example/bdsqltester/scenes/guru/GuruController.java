package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.scenes.wali_kelas.AbsenSiswaController;
import com.example.bdsqltester.scenes.wali_kelas.PrintRaporController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.io.IOException;

public class GuruController {

    @FXML
    private Label namaUser;

    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            namaUser.setText("Halo, " + user.getUsername() + "!");
        }
    }

    @FXML
    public void onInputNilaiClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("input_nilai-view.fxml"));
            Scene scene = new Scene(loader.load());
            InputNilaiController controller = loader.getController();
            controller.setUser(this.user);
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Input Nilai");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman input nilai.");
            e.printStackTrace();
        }
    }

    @FXML
    public void onAbsenSiswaClick(ActionEvent actionEvent) {
        if (user != null && user.isWaliKelas()) {
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
        } else {
            showAlert(Alert.AlertType.WARNING, "Akses Ditolak", "Fungsi ini hanya untuk Wali Kelas.");
        }
    }

    @FXML
    public void onPrintRaporClick(ActionEvent actionEvent) {
        if (user != null && user.isWaliKelas()) {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("print_rapor-view.fxml"));
                Scene scene = new Scene(loader.load());
                PrintRaporController controller = loader.getController();
                controller.setUser(this.user);
                HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
                HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Cetak Rapor");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman cetak rapor.");
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Akses Ditolak", "Fungsi ini hanya untuk Wali Kelas.");
        }
    }

    @FXML
    public void OnLogoutClick(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(loader.load());
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Login");
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,"Error", "Gagal memuat halaman login.");
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