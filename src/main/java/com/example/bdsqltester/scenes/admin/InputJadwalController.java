package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InputJadwalController {

    // --- Komponen FXML dari FXML yang diperbarui ---

    // TableView dan Kolomnya untuk menampilkan data
    @FXML private TableView<JadwalData> jadwalTable;
    @FXML private TableColumn<JadwalData, String> kelasTable;
    @FXML private TableColumn<JadwalData, String> hariTable;
    @FXML private TableColumn<JadwalData, String> mapelTable;
    @FXML private TableColumn<JadwalData, String> mulaiTable;
    @FXML private TableColumn<JadwalData, String> selesaiTable;
    @FXML private TableColumn<JadwalData, String> guruTable;

    // Komponen untuk Pencarian
    @FXML private TextField searchField;

    // Komponen untuk menampilkan detail jadwal yang dipilih (kanan atas)
    @FXML private TextField kelasField;
    @FXML private TextField guruField;
    @FXML private TextField mapelField;

    // Komponen untuk Input Data Baru (bawah)
    @FXML private ListView<Kelas> kelasList;
    @FXML private ListView<Guru> guruList;
    @FXML private ListView<Mapel> mapelList;
    @FXML private ChoiceBox<String> hariBox;
    @FXML private TextField mulaiField;
    @FXML private TextField selesaiField;


    // ObservableLists untuk menampung data
    private final ObservableList<JadwalData> jadwalDataObservableList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasObservableList = FXCollections.observableArrayList();
    private final ObservableList<Guru> guruObservableList = FXCollections.observableArrayList();
    private final ObservableList<Mapel> mapelObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupInputControls();
        loadInputData();
        setupTableView();
        loadJadwalTableData(""); // Muat semua data saat awal
    }

    private void setupTableView() {
        kelasTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaKelas()));
        hariTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHari()));
        mapelTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaMapel()));
        mulaiTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJamMulai()));
        selesaiTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getJamSelesai()));
        guruTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaGuru()));

        jadwalTable.setItems(jadwalDataObservableList);

        // Listener untuk menampilkan detail saat sebuah baris di tabel dipilih
        jadwalTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> populateForms(newSelection)
        );
    }

    private void setupInputControls() {
        kelasList.setItems(kelasObservableList);
        guruList.setItems(guruObservableList);
        mapelList.setItems(mapelObservableList);

        kelasList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Kelas kelas, boolean empty) {
                super.updateItem(kelas, empty);
                setText(empty ? "" : kelas.getNamaKelas());
            }
        });

        guruList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Guru guru, boolean empty) {
                super.updateItem(guru, empty);
                setText(empty ? "" : guru.getNamaGuru());
            }
        });

        mapelList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Mapel mapel, boolean empty) {
                super.updateItem(mapel, empty);
                setText(empty ? "" : mapel.getNamaMapel());
            }
        });

        hariBox.setItems(FXCollections.observableArrayList("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu"));
    }

    // *** FUNGSI BARU: onRefreshClick ***
    @FXML
    void onRefreshClick(ActionEvent event) {
        clearForm();
        loadJadwalTableData("");
    }

    private void loadInputData() {
        loadKelasList();
        loadGuruList();
        loadMapelList();
    }

    private void loadJadwalTableData(String keyword) {
        jadwalDataObservableList.clear();
        List<String> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT k.nama_kelas, j.hari, m.nama_mapel, j.jam_mulai, j.jam_selesai, g.nama_guru " +
                        "FROM jadwal j " +
                        "JOIN kelas k ON j.kelas_id = k.kelas_id " +
                        "JOIN guru g ON j.guru_id = g.nip " +
                        "JOIN mapel m ON j.mapel_id = m.mapel_id "
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("WHERE k.nama_kelas ILIKE ? OR m.nama_mapel ILIKE ? OR g.nama_guru ILIKE ? OR j.hari ILIKE ?");
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        sql.append(" ORDER BY j.jadwal_id");

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                jadwalDataObservableList.add(new JadwalData(
                        rs.getString("nama_kelas"),
                        rs.getString("hari"),
                        rs.getString("nama_mapel"),
                        rs.getString("jam_mulai"),
                        rs.getString("jam_selesai"),
                        rs.getString("nama_guru")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data jadwal dari database.");
            e.printStackTrace();
        }
    }

    // *** FUNGSI DIPERBARUI: populateForms ***
    private void populateForms(JadwalData jadwal) {
        if (jadwal == null) {
            return;
        }
        // Mengisi field display (kanan atas)
        kelasField.setText(jadwal.getNamaKelas());
        guruField.setText(jadwal.getNamaGuru());
        mapelField.setText(jadwal.getNamaMapel());

        // Mengisi form input (bawah)
        for (Kelas k : kelasObservableList) {
            if (k.getNamaKelas().equals(jadwal.getNamaKelas())) {
                kelasList.getSelectionModel().select(k);
                break;
            }
        }
        for (Guru g : guruObservableList) {
            if (g.getNamaGuru().equals(jadwal.getNamaGuru())) {
                guruList.getSelectionModel().select(g);
                break;
            }
        }
        for (Mapel m : mapelObservableList) {
            if (m.getNamaMapel().equals(jadwal.getNamaMapel())) {
                mapelList.getSelectionModel().select(m);
                break;
            }
        }
        hariBox.setValue(jadwal.getHari());
        mulaiField.setText(jadwal.getJamMulai());
        selesaiField.setText(jadwal.getJamSelesai());
    }

    @FXML
    void onSearchClick(ActionEvent event) {
        loadJadwalTableData(searchField.getText());
    }

    @FXML
    void onAddClick(ActionEvent event) {
        Kelas selectedKelas = kelasList.getSelectionModel().getSelectedItem();
        Guru selectedGuru = guruList.getSelectionModel().getSelectedItem();
        Mapel selectedMapel = mapelList.getSelectionModel().getSelectedItem();
        String hari = hariBox.getValue();
        String jamMulai = mulaiField.getText();
        String jamSelesai = selesaiField.getText();

        if (selectedKelas == null || selectedGuru == null || selectedMapel == null || hari == null ||
                jamMulai.trim().isEmpty() || jamSelesai.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Semua pilihan dan field untuk input baru harus diisi.");
            return;
        }

        String sql = "INSERT INTO jadwal (hari, jam_mulai, jam_selesai, mapel_id, kelas_id, guru_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hari);
            pstmt.setString(2, jamMulai);
            pstmt.setString(3, jamSelesai);
            pstmt.setInt(4, selectedMapel.getMapelId());
            pstmt.setInt(5, selectedKelas.getKelasId());
            pstmt.setString(6, selectedGuru.getNip());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal baru berhasil ditambahkan.");
            clearForm();
            loadJadwalTableData("");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan jadwal baru.\nError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Lengkap", "Fungsi untuk menyimpan perubahan belum diimplementasikan.");
    }

    @FXML
    void onRemoveClick(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Belum Lengkap", "Fungsi untuk menghapus jadwal belum diimplementasikan.");
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(new Scene(loader.load()));
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Admin Dashboard");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal kembali ke halaman admin.");
        }
    }

    private void loadKelasList() {
        kelasObservableList.clear();
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT kelas_id, nama_kelas FROM kelas ORDER BY nama_kelas");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                kelasObservableList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat daftar kelas.");
        }
    }

    private void loadGuruList() {
        guruObservableList.clear();
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT nip, nama_guru FROM guru ORDER BY nama_guru");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                guruObservableList.add(new Guru(rs.getString("nip"), rs.getString("nama_guru")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat daftar guru.");
        }
    }

    private void loadMapelList() {
        mapelObservableList.clear();
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT mapel_id, nama_mapel FROM mapel ORDER BY nama_mapel");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                mapelObservableList.add(new Mapel(rs.getInt("mapel_id"), rs.getString("nama_mapel")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat daftar mata pelajaran.");
        }
    }

    private void clearForm() {
        // Membersihkan form input data baru
        kelasList.getSelectionModel().clearSelection();
        guruList.getSelectionModel().clearSelection();
        mapelList.getSelectionModel().clearSelection();
        hariBox.setValue(null);
        mulaiField.clear();
        selesaiField.clear();

        // Membersihkan form detail dan search
        jadwalTable.getSelectionModel().clearSelection();
        kelasField.clear();
        guruField.clear();
        mapelField.clear();
        searchField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class untuk menampung data jadwal di TableView
    public static class JadwalData {
        private final String namaKelas, hari, namaMapel, jamMulai, jamSelesai, namaGuru;

        public JadwalData(String namaKelas, String hari, String namaMapel, String jamMulai, String jamSelesai, String namaGuru) {
            this.namaKelas = namaKelas;
            this.hari = hari;
            this.namaMapel = namaMapel;
            this.jamMulai = jamMulai;
            this.jamSelesai = jamSelesai;
            this.namaGuru = namaGuru;
        }

        public String getNamaKelas() { return namaKelas; }
        public String getHari() { return hari; }
        public String getNamaMapel() { return namaMapel; }
        public String getJamMulai() { return jamMulai; }
        public String getJamSelesai() { return jamSelesai; }
        public String getNamaGuru() { return namaGuru; }
    }

    // Inner classes untuk menampung data input
    public static class Kelas {
        private final int kelasId;
        private final String namaKelas;
        public Kelas(int id, String nama) { this.kelasId = id; this.namaKelas = nama; }
        public int getKelasId() { return kelasId; }
        public String getNamaKelas() { return namaKelas; }
        @Override public boolean equals(Object o) { if (o instanceof Kelas) return this.kelasId == ((Kelas) o).kelasId; return false; }
        @Override public int hashCode() { return Objects.hash(kelasId); }
    }

    public static class Guru {
        private final String nip;
        private final String namaGuru;
        public Guru(String nip, String nama) { this.nip = nip; this.namaGuru = nama; }
        public String getNip() { return nip; }
        public String getNamaGuru() { return namaGuru; }
        @Override public boolean equals(Object o) { if (o instanceof Guru) return this.nip.equals(((Guru) o).nip); return false; }
        @Override public int hashCode() { return Objects.hash(nip); }
    }

    public static class Mapel {
        private final int mapelId;
        private final String namaMapel;
        public Mapel(int id, String nama) { this.mapelId = id; this.namaMapel = nama; }
        public int getMapelId() { return mapelId; }
        public String getNamaMapel() { return namaMapel; }
        @Override public boolean equals(Object o) { if (o instanceof Mapel) return this.mapelId == ((Mapel) o).mapelId; return false; }
        @Override public int hashCode() { return Objects.hash(mapelId); }
    }
}