package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GuruJadwalController {

    @FXML private TableView<JadwalData> jadwalList;
    @FXML private TableColumn<JadwalData, String> kelasList;
    @FXML private TableColumn<JadwalData, String> mapelList;
    @FXML private TextField mapelField;
    @FXML private TextField kelasField;
    @FXML private TextField hariField;
    @FXML private TextField mulaiField;
    @FXML private TextField selesaiField;

    // PENYESUAIAN: Menggunakan dua TextField baru untuk pencarian sesuai FXML
    @FXML private TextField kelassearchField;
    @FXML private TextField mapelsearchField1;


    private User user;
    private final ObservableList<JadwalData> jadwalDataList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
        // Memuat semua data saat pertama kali dibuka
        loadJadwalData(null, null);
    }

    @FXML
    public void initialize() {
        kelasList.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaKelas()));
        mapelList.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaMapel()));

        jadwalList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showJadwalDetails(newValue)
        );

        jadwalList.setItems(jadwalDataList);
    }

    // PENYESUAIAN: Metode diubah untuk menerima dua parameter pencarian
    private void loadJadwalData(String kelasSearch, String mapelSearch) {
        jadwalDataList.clear();

        StringBuilder sql = new StringBuilder(
                "SELECT k.nama_kelas, m.nama_mapel, j.hari, j.jam_mulai, j.jam_selesai " +
                        "FROM jadwal j " +
                        "JOIN mapel m ON j.mapel_id = m.mapel_id " +
                        "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                        "WHERE m.guru_id = ? "
        );

        List<String> params = new ArrayList<>();

        if (kelasSearch != null && !kelasSearch.isEmpty()) {
            sql.append("AND LOWER(k.nama_kelas) LIKE ? ");
            params.add("%" + kelasSearch.toLowerCase() + "%");
        }
        if (mapelSearch != null && !mapelSearch.isEmpty()) {
            sql.append("AND LOWER(m.nama_mapel) LIKE ? ");
            params.add("%" + mapelSearch.toLowerCase() + "%");
        }


        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Parameter pertama selalu NIP guru
            stmt.setString(1, user.getNip());

            // Set parameter dinamis untuk pencarian
            for (int i = 0; i < params.size(); i++) {
                // Parameter dimulai dari indeks 2, setelah NIP
                stmt.setString(i + 2, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                jadwalDataList.add(new JadwalData(
                        rs.getString("nama_kelas"),
                        rs.getString("nama_mapel"),
                        rs.getString("hari"),
                        rs.getString("jam_mulai"),
                        rs.getString("jam_selesai")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data jadwal.");
        }
    }

    private void showJadwalDetails(JadwalData jadwal) {
        if (jadwal != null) {
            kelasField.setText(jadwal.getNamaKelas());
            mapelField.setText(jadwal.getNamaMapel());
            hariField.setText(jadwal.getHari());
            mulaiField.setText(jadwal.getJamMulai());
            selesaiField.setText(jadwal.getJamSelesai());
        } else {
            kelasField.clear();
            mapelField.clear();
            hariField.clear();
            mulaiField.clear();
            selesaiField.clear();
        }
    }

    @FXML
    void onSearchClick() {
        // PENYESUAIAN: Memanggil loadJadwalData dengan nilai dari kedua TextField
        loadJadwalData(kelassearchField.getText(), mapelsearchField1.getText());
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml"));
            Scene scene = new Scene(loader.load());

            GuruController guruController = loader.getController();
            guruController.setUser(this.user);

            app.getPrimaryStage().setScene(scene);
            app.getPrimaryStage().setTitle("Halaman Guru");

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal memuat halaman guru.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class JadwalData {
        private final String namaKelas;
        private final String namaMapel;
        private final String hari;
        private final String jamMulai;
        private final String jamSelesai;

        public JadwalData(String namaKelas, String namaMapel, String hari, String jamMulai, String jamSelesai) {
            this.namaKelas = namaKelas;
            this.namaMapel = namaMapel;
            this.hari = hari;
            this.jamMulai = jamMulai;
            this.jamSelesai = jamSelesai;
        }

        public String getNamaKelas() { return namaKelas; }
        public String getNamaMapel() { return namaMapel; }
        public String getHari() { return hari; }
        public String getJamMulai() { return jamMulai; }
        public String getJamSelesai() { return jamSelesai; }
    }
}