package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class InputBiodataController {

    @FXML private TableView<Siswa> siswaList;
    @FXML private TableColumn<Siswa, String> namaList;
    @FXML private TableColumn<Siswa, String> nisList;
    @FXML private TextField alamatField, ayahField, ibuField, namaField, nisField, phoneField, searchnamaField, searchnisField;
    @FXML private ChoiceBox<String> genderBox, agamaBox;
    @FXML private DatePicker tanggalField;

    private Integer currentBiodataId = null;
    private final ObservableList<Siswa> masterSiswaList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        namaList.setCellValueFactory(new PropertyValueFactory<>("namaSiswa"));
        nisList.setCellValueFactory(new PropertyValueFactory<>("nis"));
        siswaList.setItems(masterSiswaList);

        genderBox.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));
        agamaBox.setItems(FXCollections.observableArrayList("Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Khonghucu"));

        siswaList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadBiodataForSiswa(newSelection);
                    } else {
                        clearFields();
                    }
                });

        loadSiswaMasterList("", "");
    }

    private void loadSiswaMasterList(String namaFilter, String nisFilter) {
        masterSiswaList.clear();
        String sql = "SELECT nis, nama_siswa FROM siswa WHERE nama_siswa ILIKE ? AND nis ILIKE ? ORDER BY nama_siswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + namaFilter + "%");
            pstmt.setString(2, "%" + nisFilter + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                masterSiswaList.add(new Siswa(rs.getString("nis"), rs.getString("nama_siswa")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat daftar siswa: " + e.getMessage());
        }
    }

    private void loadBiodataForSiswa(Siswa siswa) {
        String sql = "SELECT * FROM biodata WHERE siswa_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, siswa.getNis());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentBiodataId = rs.getInt("biodata_id");
                namaField.setText(rs.getString("nama_siswa"));
                nisField.setText(rs.getString("siswa_id"));
                tanggalField.setValue(LocalDate.parse(rs.getString("tanggal_lahir")));
                genderBox.setValue(rs.getString("jenis_kelamin"));
                phoneField.setText(rs.getString("telp_orang_tua")); // Menggunakan nama kolom yang benar
                ibuField.setText(rs.getString("ibu")); // Menggunakan nama kolom yang benar (lowercase)
                ayahField.setText(rs.getString("ayah")); // Menggunakan nama kolom yang benar (lowercase)
                alamatField.setText(rs.getString("alamat"));
                agamaBox.setValue(rs.getString("agama"));
            } else {
                clearFields();
                currentBiodataId = null;
                namaField.setText(siswa.getNamaSiswa());
                nisField.setText(siswa.getNis());
                showAlert(Alert.AlertType.INFORMATION, "Info", "Siswa ini belum memiliki biodata. Silakan isi form.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mencari biodata: " + e.getMessage());
        }
    }

    @FXML
    void onSearchClick(ActionEvent event) {
        loadSiswaMasterList(searchnamaField.getText(), searchnisField.getText());
    }

    @FXML
    void onAddClick(ActionEvent event) {
        if (currentBiodataId != null) {
            showAlert(Alert.AlertType.WARNING, "Operasi Gagal", "Siswa ini sudah memiliki biodata. Gunakan 'Save' untuk mengubah.");
            return;
        }
        if (isAnyFieldEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Semua field formulir harus diisi.");
            return;
        }

        String sql = "INSERT INTO biodata (siswa_id, nama_siswa, tanggal_lahir, jenis_kelamin, alamat, agama, telp_orang_tua, Ibu, Ayah) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String nis = nisField.getText();
            String tanggalLahir = tanggalField.getValue().toString();

            pstmt.setString(1, nis);
            pstmt.setString(2, namaField.getText());
            pstmt.setString(3, tanggalLahir);
            pstmt.setString(4, genderBox.getValue());
            pstmt.setString(5, alamatField.getText());
            pstmt.setString(6, agamaBox.getValue());
            pstmt.setString(7, phoneField.getText());
            pstmt.setString(8, ibuField.getText());
            pstmt.setString(9, ayahField.getText());

            if (pstmt.executeUpdate() > 0) {
                updatePasswordSiswa(conn, nis, tanggalLahir);
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Biodata berhasil ditambahkan dan password siswa diatur ke tanggal lahir.");
                loadBiodataForSiswa(siswaList.getSelectionModel().getSelectedItem());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan data.\nError: " + e.getMessage());
        }
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        if (currentBiodataId == null) {
            showAlert(Alert.AlertType.WARNING, "Operasi Gagal", "Siswa ini belum memiliki biodata. Gunakan 'Add'.");
            return;
        }
        if (isAnyFieldEmpty()) return;

        String sql = "UPDATE biodata SET siswa_id = ?, nama_siswa = ?, tanggal_lahir = ?, jenis_kelamin = ?, alamat = ?, telp_orang_tua = ?, Ibu = ?, Ayah = ?, agama = ? WHERE biodata_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String nis = nisField.getText();
            String tanggalLahir = tanggalField.getValue().toString();

            pstmt.setString(1, nis);
            pstmt.setString(2, namaField.getText());
            pstmt.setString(3, tanggalLahir);
            pstmt.setString(4, genderBox.getValue());
            pstmt.setString(5, alamatField.getText());
            pstmt.setString(6, phoneField.getText());
            pstmt.setString(7, ibuField.getText());
            pstmt.setString(8, ayahField.getText());
            pstmt.setString(9, agamaBox.getValue());
            pstmt.setInt(10, currentBiodataId);

            if (pstmt.executeUpdate() > 0) {
                updatePasswordSiswa(conn, nis, tanggalLahir);
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Biodata berhasil diperbarui dan password siswa telah disesuaikan.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data.\nError: " + e.getMessage());
        }
    }

    // Metode baru untuk update password
    private void updatePasswordSiswa(Connection conn, String nis, String password) throws SQLException {
        String sql = "UPDATE siswa SET password = ? WHERE nis = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, password);
            pstmt.setString(2, nis);
            pstmt.executeUpdate();
        }
    }

    @FXML
    void onDeleteClick(ActionEvent event) {
        if (currentBiodataId == null) {
            showAlert(Alert.AlertType.WARNING, "Tidak Ada Data", "Tidak ada biodata untuk dihapus.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus biodata untuk " + namaField.getText() + "?", ButtonType.YES, ButtonType.NO);
        if (confirmation.showAndWait().filter(ButtonType.YES::equals).isPresent()) {
            String sql = "DELETE FROM biodata WHERE biodata_id = ?";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentBiodataId);
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Biodata berhasil dihapus.");
                Siswa selected = siswaList.getSelectionModel().getSelectedItem();
                clearFields();
                if(selected != null) {
                    namaField.setText(selected.getNamaSiswa());
                    nisField.setText(selected.getNis());
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data.\nError: " + e.getMessage());
            }
        }
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(new Scene(loader.load()));
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Admin Dashboard");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman admin.");
        }
    }

    private void clearFields() {
        currentBiodataId = null;
        nisField.clear();
        namaField.clear();
        tanggalField.setValue(null);
        genderBox.setValue(null);
        phoneField.clear();
        ibuField.clear();
        ayahField.clear();
        alamatField.clear();
        agamaBox.setValue(null);
    }

    private boolean isAnyFieldEmpty() {
        boolean empty = namaField.getText().trim().isEmpty() || nisField.getText().trim().isEmpty() || tanggalField.getValue() == null ||
                genderBox.getValue() == null || phoneField.getText().trim().isEmpty() || ibuField.getText().trim().isEmpty() ||
                ayahField.getText().trim().isEmpty() || alamatField.getText().trim().isEmpty() ||
                agamaBox.getValue() == null;
        if (empty) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Semua field formulir harus diisi.");
        }
        return empty;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Siswa {
        private final String nis;
        private final String namaSiswa;

        public Siswa(String nis, String namaSiswa) {
            this.nis = nis;
            this.namaSiswa = namaSiswa;
        }
        public String getNis() { return nis; }
        public String getNamaSiswa() { return namaSiswa; }
    }
}