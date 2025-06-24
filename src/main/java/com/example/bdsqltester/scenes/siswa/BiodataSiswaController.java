package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BiodataSiswaController implements SiswaDataReceiver {

    // Nama variabel di sini tidak harus sama dengan nama kolom, yang penting adalah @FXML
    @FXML private Label namaLabel, nisLabel, ttlLabel, jkLabel, agamaLabel, alamatLabel, telpLabel, ayahLabel, ibuLabel;

    private User currentUser;

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        loadBiodata();
    }

    private void loadBiodata() {
        String sql = "SELECT * FROM biodata WHERE siswa_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUser.getId()); // Mencari berdasarkan NIS
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Mengisi data dari objek User yang sudah ada
                namaLabel.setText(currentUser.getUsername());
                nisLabel.setText(currentUser.getId());

                // --- PERBAIKAN DI SINI ---
                // Mengambil data dari ResultSet menggunakan nama kolom yang benar dari database Anda

                // Kolom 'tempat_lahir' tidak ada di tabel Anda, jadi kita hanya tampilkan tanggal lahir
                String tanggalLahir = rs.getString("tanggal_lahir");
                ttlLabel.setText(tanggalLahir != null ? tanggalLahir : "-");

                jkLabel.setText(rs.getString("jenis_kelamin"));
                agamaLabel.setText(rs.getString("agama"));
                alamatLabel.setText(rs.getString("alamat"));

                // Menggunakan kolom 'telp_orang_tua'
                telpLabel.setText(rs.getString("telp_orang_tua"));

                // Menggunakan kolom 'ayah' dan 'ibu'
                ayahLabel.setText(rs.getString("ayah"));
                ibuLabel.setText(rs.getString("ibu"));

            } else {
                // Jika tidak ada data biodata ditemukan untuk siswa tersebut
                namaLabel.setText(currentUser.getUsername());
                nisLabel.setText(currentUser.getId());
                ttlLabel.setText("Data tidak ditemukan");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Tampilkan error jika terjadi masalah saat query
            ttlLabel.setText("Gagal memuat data");
        }
    }

    @FXML
    void onBackClick(ActionEvent event) {
        SiswaControllerUtil.backToSiswaDashboard(currentUser);
    }
}