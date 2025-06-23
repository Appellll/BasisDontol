package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.Biodata;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BiodataController {

    // Semua Label untuk menampilkan data
    @FXML private Label namaLabel;
    @FXML private Label tanggalLahirLabel;
    @FXML private Label jenisKelaminLabel;
    @FXML private Label alamatLabel;
    @FXML private Label agamaLabel;
    @FXML private Label telpLabel;
    @FXML private Label ayahLabel;
    @FXML private Label ibuLabel;

    // fx:id untuk tombol kembali
    @FXML private Button backButton;

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadBiodata();
    }

    private void loadBiodata() {
        // Logika untuk mencari NIS siswa berdasarkan username
        String findNisSql = "SELECT nis FROM siswa WHERE nama_siswa = ?";
        // Logika untuk mengambil biodata berdasarkan NIS (siswa_id)
        String biodataSql = "SELECT * FROM biodata WHERE siswa_id = ?";

        // Mengambil koneksi di dalam metode agar lebih aman
        try (Connection connection = MainDataSource.getConnection();
             PreparedStatement findNisStmt = connection.prepareStatement(findNisSql)) {

            findNisStmt.setString(1, this.user.getUsername());
            ResultSet rsNis = findNisStmt.executeQuery();

            if (rsNis.next()) {
                String nis = rsNis.getString("nis");
                // Melakukan query kedua untuk mengambil biodata
                try (PreparedStatement biodataStmt = connection.prepareStatement(biodataSql)) {
                    biodataStmt.setString(1, nis);
                    ResultSet rsBiodata = biodataStmt.executeQuery();

                    if (rsBiodata.next()) {
                        // Jika data ditemukan, buat objek Biodata dan isi semua label
                        Biodata data = new Biodata(rsBiodata);
                        namaLabel.setText(": " + data.namaSiswa);
                        tanggalLahirLabel.setText(": " + data.tanggalLahir);
                        jenisKelaminLabel.setText(": " + data.jenisKelamin);
                        alamatLabel.setText(": " + data.alamat);
                        agamaLabel.setText(": " + data.agama);
                        telpLabel.setText(": " + data.telpOrangTua);
                        ayahLabel.setText(": " + data.namaAyah);
                        ibuLabel.setText(": " + data.namaIbu);
                    } else {
                        showAlert("Info", "Data biodata untuk siswa ini tidak ditemukan di tabel biodata.");
                    }
                }
            } else {
                showAlert("Info", "Data siswa tidak ditemukan di tabel master siswa.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal mengambil data biodata dari database.");
        }
    }

    @FXML
    void onBackClick(ActionEvent event) throws IOException {
        // Logika untuk kembali ke menu utama siswa
        Stage currentStage = (Stage) backButton.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
        Scene scene = new Scene(loader.load());

        // Kirim kembali data user agar sapaan nama tetap ada
        SiswaController siswaController = loader.getController();
        siswaController.setUser(this.user);

        currentStage.setScene(scene);
        currentStage.setTitle("Menu Siswa"); // Mengembalikan judul jendela
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}