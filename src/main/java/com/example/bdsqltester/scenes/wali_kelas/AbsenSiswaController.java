package com.example.bdsqltester.scenes.wali_kelas;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.scenes.guru.GuruController; // <-- INI BARIS YANG PERLU DITAMBAHKAN
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AbsenSiswaController {

    // Komponen FXML Sesuai View Anda
    @FXML private TableView<AbsenData> absenList;
    @FXML private TableColumn<AbsenData, String> tanggalList;
    @FXML private TableColumn<AbsenData, String> siswaList;
    @FXML private TableColumn<AbsenData, String> guruList;
    @FXML private TableColumn<AbsenData, String> statusList; // Diubah ke String
    @FXML private TextField namaField;
    @FXML private ChoiceBox<String> statusBox;

    private User currentUser;
    private int kelasId;
    private final ObservableList<AbsenData> absenDataObservableList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.currentUser = user;
        loadKelasInfoAndData();
    }

    @FXML
    public void initialize() {
        tanggalList.setCellValueFactory(cellData -> cellData.getValue().tanggalProperty());
        siswaList.setCellValueFactory(cellData -> cellData.getValue().namaSiswaProperty());
        guruList.setCellValueFactory(cellData -> cellData.getValue().namaGuruProperty());
        statusList.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        absenList.setItems(absenDataObservableList);

        absenList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateFields(newSelection);
                    }
                });

        statusBox.setItems(FXCollections.observableArrayList("Hadir", "Sakit", "Izin", "Alfa"));
    }

    private void populateFields(AbsenData data) {
        namaField.setText(data.getNamaSiswa());
        statusBox.setValue(data.getStatus());
    }

    private void loadKelasInfoAndData() {
        String sql = "SELECT kelas_id FROM kelas WHERE guru_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser.getNip());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.kelasId = rs.getInt("kelas_id");
                loadAbsenData();
            } else {
                showAlert(Alert.AlertType.WARNING, "Tidak Ada Kelas", "Anda tidak terdaftar sebagai wali kelas.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAbsenData() {
        absenDataObservableList.clear();
        String sql = "SELECT a.absen_id, a.tanggal, s.nama_siswa, g.nama_guru, a.status_absen " +
                "FROM absen a " +
                "JOIN siswa s ON a.siswa_id = s.nis " +
                "JOIN guru g ON a.guru_id = g.nip " +
                "WHERE s.kelas_id = ? ORDER BY a.tanggal DESC, s.nama_siswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.kelasId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                absenDataObservableList.add(new AbsenData(
                        rs.getInt("absen_id"),
                        rs.getString("tanggal"),
                        rs.getString("nama_siswa"),
                        rs.getString("nama_guru"),
                        rs.getString("status_absen")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data absensi.");
        }
    }

    @FXML
    void onAddClick(ActionEvent event) {
        String namaSiswa = namaField.getText();
        String status = statusBox.getValue();
        String tanggalHariIni = LocalDate.now().toString();

        if (namaSiswa.trim().isEmpty() || status == null) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Nama siswa dan status harus diisi.");
            return;
        }

        String siswaNis = null;
        String checkSiswaSql = "SELECT nis FROM siswa WHERE nama_siswa = ? AND kelas_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSiswaSql)) {
            checkStmt.setString(1, namaSiswa);
            checkStmt.setInt(2, this.kelasId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                siswaNis = rs.getString("nis");
            } else {
                showAlert(Alert.AlertType.ERROR, "Siswa Tidak Ditemukan", "Siswa dengan nama '" + namaSiswa + "' tidak ditemukan di kelas Anda.");
                return;
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mencari data siswa.");
            e.printStackTrace();
            return;
        }

        String sql = "INSERT INTO absen (tanggal, siswa_id, guru_id, status_absen) VALUES (?, ?, ?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tanggalHariIni);
            pstmt.setString(2, siswaNis);
            pstmt.setString(3, currentUser.getNip());
            pstmt.setString(4, status);

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Absensi untuk " + namaSiswa + " berhasil ditambahkan.");
            loadAbsenData();

        } catch (SQLException e) {
            if(e.getSQLState().equals("23505")) {
                showAlert(Alert.AlertType.ERROR, "Data Duplikat", "Siswa ini sudah diabsen pada tanggal yang sama.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan data absensi.");
            }
            e.printStackTrace();
        }
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        AbsenData selected = absenList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih data absensi dari tabel untuk disimpan.");
            return;
        }
        if (statusBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Status tidak boleh kosong.");
            return;
        }

        String sql = "UPDATE absen SET status_absen = ? WHERE absen_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, statusBox.getValue());
            pstmt.setInt(2, selected.getAbsenId());
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data absensi berhasil diperbarui.");
            loadAbsenData();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan perubahan.");
        }
    }

    @FXML
    void onDeleteClick(ActionEvent event) {
        AbsenData selected = absenList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih data absensi untuk dihapus.");
            return;
        }
        String sql = "DELETE FROM absen WHERE absen_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, selected.getAbsenId());
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data absensi berhasil dihapus.");
            loadAbsenData();
            namaField.clear();
            statusBox.setValue(null);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data.");
        }
    }

    @FXML
    void onBackClick(ActionEvent event) {
        if (this.currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Data user tidak ditemukan.");
            return;
        }
        try {
            // ===== PERUBAHAN DI SINI =====
            // Kembali ke halaman wali kelas, bukan halaman guru umum
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("wali_kelas-view.fxml"));
            Scene scene = new Scene(loader.load());
            WaliKelasController controller = loader.getController();
            controller.setUser(this.currentUser);
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Wali Kelas Dashboard");
            // ============================
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman wali kelas.");
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class AbsenData {
        private final SimpleIntegerProperty absenId;
        private final SimpleStringProperty tanggal;
        private final SimpleStringProperty namaSiswa;
        private final SimpleStringProperty namaGuru;
        private final SimpleStringProperty status;

        public AbsenData(int absenId, String tanggal, String namaSiswa, String namaGuru, String status) {
            this.absenId = new SimpleIntegerProperty(absenId);
            this.tanggal = new SimpleStringProperty(tanggal);
            this.namaSiswa = new SimpleStringProperty(namaSiswa);
            this.namaGuru = new SimpleStringProperty(namaGuru);
            this.status = new SimpleStringProperty(status);
        }

        public int getAbsenId() { return absenId.get(); }
        public String getTanggal() { return tanggal.get(); }
        public String getNamaSiswa() { return namaSiswa.get(); }
        public String getNamaGuru() { return namaGuru.get(); }
        public String getStatus() { return status.get(); }

        public SimpleStringProperty tanggalProperty() { return tanggal; }
        public SimpleStringProperty namaSiswaProperty() { return namaSiswa; }
        public SimpleStringProperty namaGuruProperty() { return namaGuru; }
        public SimpleStringProperty statusProperty() { return status; }
    }
}