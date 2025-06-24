package com.example.bdsqltester.scenes.guru;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InputNilaiController {

    @FXML private TableView<NilaiData> tableNilai;
    @FXML private TableColumn<NilaiData, String> nisList;
    @FXML private TableColumn<NilaiData, String> kelasList;
    @FXML private TableColumn<NilaiData, String> namaList;
    @FXML private TableColumn<NilaiData, String> mapelList;
    @FXML private TableColumn<NilaiData, String> tipeList;
    @FXML private TableColumn<NilaiData, Integer> nilaiList;
    @FXML private TableColumn<NilaiData, String> tanggalList;
    @FXML private TableColumn<NilaiData, String> tahunajaranList;

    @FXML private ChoiceBox<MapelItem> mapelBox;
    @FXML private ChoiceBox<String> tipeBox;
    @FXML private ChoiceBox<KelasItem> kelasBox;
    @FXML private ChoiceBox<SiswaItem> siswaBox;
    @FXML private TextField nilaiField;

    private User user;
    private Connection connection;
    private final ObservableList<NilaiData> nilaiDataList = FXCollections.observableArrayList();
    private NilaiData selectedNilaiData;

    public void setUser(User user) {
        if (user == null || user.getNip() == null || user.getNip().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Kesalahan Kritis", "NIP Guru tidak valid.");
            return;
        }
        this.user = user;
        if (this.connection != null) {
            initializeData();
        }
    }

    @FXML
    public void initialize() {
        try {
            this.connection = MainDataSource.getConnection();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Connection Error", "Gagal terhubung.");
            e.printStackTrace();
            return;
        }

        nisList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNis()));
        kelasList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaKelas()));
        namaList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaSiswa()));
        mapelList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNamaMapel()));
        tipeList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipeNilai()));
        nilaiList.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getNilaiSiswa()).asObject());
        tanggalList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTanggalInput()));
        tahunajaranList.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTahunAjaran()));

        tableNilai.setItems(nilaiDataList);
        tableNilai.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> populateFieldsFromTable(val));
    }

    private void initializeData() {
        loadMapelBox();
        loadKelasBox();
        tipeBox.setItems(FXCollections.observableArrayList("Tugas", "UH", "UTS", "UAS"));
        loadNilaiTable(null);

        kelasBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) loadSiswaBox(val.getId());
        });
        mapelBox.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) loadNilaiTable(val);
        });
    }

    private void loadNilaiTable(MapelItem mapelFilter) {
        nilaiDataList.clear();

        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT n.nilai_id, s.nis, s.nama_siswa, s.tahun_ajaran, k.nama_kelas, m.nama_mapel, n.tipe_nilai, n.nilai_siswa, n.tanggal_input " +
                        "FROM nilai n " +
                        "LEFT JOIN siswa s ON n.siswa_id = s.nis " +
                        "LEFT JOIN kelas k ON s.kelas_id = k.kelas_id " +
                        "LEFT JOIN mapel m ON n.mapel_id = m.mapel_id " +
                        "WHERE n.guru_id = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(user.getNip());

        if (mapelFilter != null && mapelFilter.getId() != 0) {
            sqlBuilder.append("AND n.mapel_id = ? ");
            params.add(mapelFilter.getId());
        }
        sqlBuilder.append("ORDER BY m.nama_mapel, s.nama_siswa");

        try (PreparedStatement stmt = connection.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                nilaiDataList.add(new NilaiData(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Gagal memuat data nilai.");
        }
    }

    private void loadMapelBox() {
        ObservableList<MapelItem> mapelItems = FXCollections.observableArrayList();
        mapelItems.add(new MapelItem(0, "Semua Mapel"));

        String sql = "SELECT mapel_id, nama_mapel FROM mapel WHERE guru_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getNip());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                mapelItems.add(new MapelItem(rs.getInt("mapel_id"), rs.getString("nama_mapel")));
            }
            mapelBox.setItems(mapelItems);
            mapelBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadKelasBox() {
        ObservableList<KelasItem> kelasItems = FXCollections.observableArrayList();
        String sql = "SELECT kelas_id, nama_kelas FROM kelas ORDER BY nama_kelas";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                kelasItems.add(new KelasItem(rs.getInt("kelas_id"), rs.getString("nama_kelas")));
            }
            kelasBox.setItems(kelasItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSiswaBox(int kelasId) {
        ObservableList<SiswaItem> siswaItems = FXCollections.observableArrayList();
        String sql = "SELECT nis, nama_siswa FROM siswa WHERE kelas_id = ? ORDER BY nama_siswa";
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, kelasId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                siswaItems.add(new SiswaItem(rs.getString("nis"), rs.getString("nama_siswa")));
            }
            siswaBox.setItems(siswaItems);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onAddClick() {
        SiswaItem selectedSiswa = siswaBox.getValue();
        MapelItem selectedMapel = mapelBox.getValue();
        String tipe = tipeBox.getValue();
        String nilaiStr = nilaiField.getText();

        if (selectedSiswa == null || selectedMapel == null || selectedMapel.getId() == 0 || tipe == null || nilaiStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap pilih mapel, kelas, siswa, tipe, dan isi nilai.");
            return;
        }

        try {
            String checkSql = "SELECT COUNT(*) FROM nilai WHERE siswa_id = ? AND mapel_id = ? AND tipe_nilai = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setString(1, selectedSiswa.getId());
                checkStmt.setInt(2, selectedMapel.getId());
                checkStmt.setString(3, tipe);

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showAlert(Alert.AlertType.ERROR, "Data Duplikat", "Gagal menambahkan. Data nilai untuk siswa, mata pelajaran, dan tipe yang sama sudah ada.");
                    return;
                }
            }

            String insertSql = "INSERT INTO nilai (tipe_nilai, siswa_id, guru_id, mapel_id, nilai_siswa) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setString(1, tipe);
                insertStmt.setString(2, selectedSiswa.getId());
                insertStmt.setString(3, user.getNip());
                insertStmt.setInt(4, selectedMapel.getId());
                insertStmt.setInt(5, Integer.parseInt(nilaiStr));

                insertStmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nilai berhasil ditambahkan!");
                loadNilaiTable(mapelBox.getValue());
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Simpan", "Gagal memproses data ke database.");
        }
    }

    private void populateFieldsFromTable(NilaiData data) {
        this.selectedNilaiData = data;

        if (data == null) {
            clearFields();
            return;
        }

        nilaiField.setText(String.valueOf(data.getNilaiSiswa()));
        tipeBox.setValue(data.getTipeNilai());

        MapelItem mapelToSelect = null;
        for (MapelItem item : mapelBox.getItems()) {
            if (item.toString().equals(data.getNamaMapel())) {
                mapelToSelect = item;
                break;
            }
        }
        mapelBox.setValue(mapelToSelect);

        KelasItem kelasToSelect = null;
        for (KelasItem item : kelasBox.getItems()) {
            if (item.toString().equals(data.getNamaKelas())) {
                kelasToSelect = item;
                break;
            }
        }
        kelasBox.setValue(kelasToSelect);

        SiswaItem siswaToSelect = null;
        for (SiswaItem item : siswaBox.getItems()) {
            if (item.toString().equals(data.getNamaSiswa())) {
                siswaToSelect = item;
                break;
            }
        }
        siswaBox.setValue(siswaToSelect);
    }

    @FXML
    void onSaveClick() {
        if (selectedNilaiData == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data dari tabel yang ingin diubah.");
            return;
        }

        SiswaItem selectedSiswa = siswaBox.getValue();
        MapelItem selectedMapel = mapelBox.getValue();
        String tipe = tipeBox.getValue();
        String nilaiStr = nilaiField.getText();

        if (selectedSiswa == null || selectedMapel == null || selectedMapel.getId() == 0 || tipe == null || nilaiStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap pastikan semua field terisi dengan benar.");
            return;
        }

        String sql = "UPDATE nilai SET tipe_nilai = ?, siswa_id = ?, mapel_id = ?, nilai_siswa = ? WHERE nilai_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tipe);
            stmt.setString(2, selectedSiswa.getId());
            stmt.setInt(3, selectedMapel.getId());
            stmt.setInt(4, Integer.parseInt(nilaiStr));
            stmt.setInt(5, selectedNilaiData.getNilaiId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Nilai berhasil diperbarui!");
                loadNilaiTable(mapelBox.getValue());
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Tidak ada data yang cocok untuk diperbarui.");
            }

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Simpan", "Gagal memperbarui nilai di database.");
        }
    }

    @FXML
    void onDeleteClick() {
        if (selectedNilaiData == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data dari tabel yang ingin dihapus.");
            return;
        }

        String sql = "DELETE FROM nilai WHERE nilai_id = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, selectedNilaiData.getNilaiId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data berhasil dihapus.");
                loadNilaiTable(mapelBox.getValue());
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Data tidak ditemukan untuk dihapus.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }


    private void clearFields() {
        tableNilai.getSelectionModel().clearSelection();
        selectedNilaiData = null;

        kelasBox.getSelectionModel().clearSelection();
        mapelBox.getSelectionModel().selectFirst();
        if (siswaBox != null) siswaBox.getItems().clear();
        tipeBox.getSelectionModel().clearSelection();
        nilaiField.clear();
    }

    @FXML
    void onBackClick(ActionEvent event) {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance(); //
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml")); //
            Scene scene = new Scene(loader.load()); //

            // Mengambil controller dari halaman guru dan mengirimkan kembali data user
            GuruController guruController = loader.getController(); //
            guruController.setUser(this.user); //

            // Mengatur scene utama untuk kembali ke halaman guru
            app.getPrimaryStage().setScene(scene); //
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

    // --- INNER CLASSES ---
    private static class MapelItem {
        private final int id; private final String name;
        public MapelItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }
    private static class KelasItem {
        private final int id; private final String name;
        public KelasItem(int id, String name) { this.id = id; this.name = name; }
        public int getId() { return id; }
        @Override public String toString() { return name; }
    }

    private static class SiswaItem {
        private final String id; private final String name;
        public SiswaItem(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        @Override public String toString() { return name; }
    }

    public static class NilaiData {
        private final int nilaiId, nilaiSiswa;
        private final String nis, namaSiswa, tahunAjaran, namaKelas, namaMapel, tipeNilai, tanggalInput;

        public NilaiData(ResultSet rs) throws SQLException {
            this.nilaiId = rs.getInt("nilai_id");
            this.nis = rs.getString("nis");
            this.namaSiswa = rs.getString("nama_siswa");
            this.tahunAjaran = rs.getString("tahun_ajaran");
            this.namaKelas = rs.getString("nama_kelas");
            this.namaMapel = rs.getString("nama_mapel");
            this.tipeNilai = rs.getString("tipe_nilai");
            this.nilaiSiswa = rs.getInt("nilai_siswa");
            this.tanggalInput = rs.getString("tanggal_input");
        }
        public int getNilaiId() { return nilaiId; }
        public String getNis() { return nis; }
        public int getNilaiSiswa() { return nilaiSiswa; }
        public String getNamaSiswa() { return namaSiswa; }
        public String getTahunAjaran() { return tahunAjaran; }
        public String getNamaKelas() { return namaKelas; }
        public String getNamaMapel() { return namaMapel; }
        public String getTipeNilai() { return tipeNilai; }
        public String getTanggalInput() { return tanggalInput; }
    }
}