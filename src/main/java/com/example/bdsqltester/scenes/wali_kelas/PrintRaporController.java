package com.example.bdsqltester.scenes.wali_kelas;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.scenes.guru.GuruController;
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

public class PrintRaporController {

    @FXML private ListView<SiswaRapor> siswaList;
    @FXML private TextField namaField;
    @FXML private TextField kelasField;
    @FXML private TextField nisField;
    @FXML private TableView<RaporEntry> raporTable;
    @FXML private TableColumn<RaporEntry, String> mapelRapor;
    @FXML private TableColumn<RaporEntry, Integer> nilaiRapor;
    @FXML private TableColumn<RaporEntry, Integer> kkmRapor;
    @FXML private TableColumn<RaporEntry, String> statusRapor;

    private User currentUser;
    private int kelasId;
    private String namaKelas;
    private final ObservableList<SiswaRapor> siswaRaporObservableList = FXCollections.observableArrayList();
    private final ObservableList<RaporEntry> raporDataObservableList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.currentUser = user;
        loadKelasInfoAndSiswa();
    }

    @FXML
    public void initialize() {
        siswaList.setItems(siswaRaporObservableList);
        siswaList.setCellFactory(lv -> new ListCell<SiswaRapor>() {
            @Override
            protected void updateItem(SiswaRapor item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getNamaSiswa());
            }
        });

        siswaList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateFields(newSelection);
                        loadRaporData(newSelection.getNis());
                    } else {
                        // Kosongkan form dan tabel jika tidak ada siswa yang dipilih
                        namaField.clear();
                        nisField.clear();
                        kelasField.clear();
                        raporDataObservableList.clear();
                    }
                });

        raporTable.setItems(raporDataObservableList);
        mapelRapor.setCellValueFactory(cellData -> cellData.getValue().namaMapelProperty());
        nilaiRapor.setCellValueFactory(cellData -> cellData.getValue().nilaiProperty().asObject());
        kkmRapor.setCellValueFactory(cellData -> cellData.getValue().kkmProperty().asObject());
        statusRapor.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    }

    private void loadRaporData(String nisSiswa) {
        raporDataObservableList.clear();
        String sql = "SELECT m.nama_mapel, n.nilai_siswa, m.kkm_mapel " +
                "FROM nilai n " +
                "JOIN mapel m ON n.mapel_id = m.mapel_id " +
                "WHERE n.siswa_id = ?";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nisSiswa);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String namaMapel = rs.getString("nama_mapel");
                int nilai = rs.getInt("nilai_siswa");
                int kkm = rs.getInt("kkm_mapel");
                String status = (nilai >= kkm) ? "Tuntas" : "Belum Tuntas";

                raporDataObservableList.add(new RaporEntry(namaMapel, nilai, kkm, status));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat detail rapor siswa.");
        }
    }

    private void populateFields(SiswaRapor siswa) {
        namaField.setText(siswa.getNamaSiswa());
        nisField.setText(siswa.getNis());
        kelasField.setText(this.namaKelas);
    }

    private void loadKelasInfoAndSiswa() {
        String sql = "SELECT kelas_id, nama_kelas FROM kelas WHERE guru_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentUser.getNip());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                this.kelasId = rs.getInt("kelas_id");
                this.namaKelas = rs.getString("nama_kelas");
                loadSiswaData();
            } else {
                showAlert(Alert.AlertType.WARNING, "Tidak Ada Kelas", "Anda tidak terdaftar sebagai wali kelas.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSiswaData() {
        siswaRaporObservableList.clear();
        String sql = "SELECT nis, nama_siswa FROM siswa WHERE kelas_id = ? ORDER BY nama_siswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, this.kelasId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                siswaRaporObservableList.add(new SiswaRapor(
                        rs.getString("nis"),
                        rs.getString("nama_siswa")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa.");
        }
    }

    @FXML
    void onPrintClick(ActionEvent event) {
        SiswaRapor selected = siswaList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih siswa dari daftar untuk dicetak rapornya.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Siap", "Mencetak rapor untuk: " + selected.getNamaSiswa() + "\n(Fungsionalitas print belum diimplementasikan).");
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml"));
            Scene scene = new Scene(loader.load());
            GuruController controller = loader.getController();
            controller.setUser(this.currentUser);
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Guru Dashboard");
        } catch (IOException e) {
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

    public static class RaporEntry {
        private final SimpleStringProperty namaMapel;
        private final SimpleIntegerProperty nilai;
        private final SimpleIntegerProperty kkm;
        private final SimpleStringProperty status;

        public RaporEntry(String namaMapel, int nilai, int kkm, String status) {
            this.namaMapel = new SimpleStringProperty(namaMapel);
            this.nilai = new SimpleIntegerProperty(nilai);
            this.kkm = new SimpleIntegerProperty(kkm);
            this.status = new SimpleStringProperty(status);
        }

        public SimpleStringProperty namaMapelProperty() { return namaMapel; }
        public SimpleIntegerProperty nilaiProperty() { return nilai; }
        public SimpleIntegerProperty kkmProperty() { return kkm; }
        public SimpleStringProperty statusProperty() { return status; }
    }

    public static class SiswaRapor {
        private final String nis;
        private final String namaSiswa;

        public SiswaRapor(String nis, String namaSiswa) {
            this.nis = nis;
            this.namaSiswa = namaSiswa;
        }

        public String getNis() { return nis; }
        public String getNamaSiswa() { return namaSiswa; }
    }
}