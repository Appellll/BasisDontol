package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import java.io.IOException;

public class SiswaController {

    @FXML
    private Label namaSiswaLabel;
    private User currentUser;

    public void setUser(User user) {
        this.currentUser = user;
        if (currentUser != null) {
            namaSiswaLabel.setText("Selamat Datang, " + currentUser.getUsername() + "!");
        }
    }

    @FXML
    void onLihatJadwalClick(ActionEvent event) {
        loadScene("jadwal-siswa-view.fxml", "Jadwal Pelajaran");
    }

    @FXML
    void onLihatNilaiClick(ActionEvent event) {
        loadScene("nilai-siswa-view.fxml", "Daftar Nilai");
    }

    @FXML
    void onLihatRaporClick(ActionEvent event) {
        loadScene("rapor_siswa-view.fxml", "Rapor Siswa");
    }
    @FXML
    void onLihatBiodataClick(ActionEvent event) {
        loadScene("biodata-siswa-view.fxml", "Biodata Siswa");
    }

    @FXML
    void onLihatSppClick(ActionEvent event) {
        loadScene("spp-siswa-view.fxml", "Status Pembayaran SPP");
    }

    private void loadScene(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            if (loader.getController() instanceof SiswaDataReceiver) {
                SiswaDataReceiver controller = loader.getController();
                controller.setUser(this.currentUser);
            }

            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle(title);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman " + title + ".");
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