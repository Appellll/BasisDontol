package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InputMapelController {

    // Komponen FXML
    @FXML private TableView<MapelData> mapelList;
    @FXML private TableColumn<MapelData, String> mapelTable;
    @FXML private TableColumn<MapelData, String> guruTable;
    @FXML private TableColumn<MapelData, Integer> kkmTable;
    @FXML private TextField mapelField;
    @FXML private TextField guruField;
    @FXML private TextField kkmField;
    @FXML private TextField searchField;

    private final ObservableList<MapelData> mapelDataObservableList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup tabel
        mapelTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaMapel()));
        guruTable.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNamaGuru()));
        kkmTable.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getKkm()).asObject());
        mapelList.setItems(mapelDataObservableList);

        // Listener untuk mengisi form saat item di tabel dipilih
        mapelList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateFields(newSelection);
                    }
                }
        );

        // Muat data awal
        loadMapelData("");
    }

    private void loadMapelData(String keyword) {
        mapelDataObservableList.clear();
        String sql = "SELECT m.mapel_id, m.nama_mapel, m.kkm_mapel, g.nip, g.nama_guru " +
                "FROM mapel m LEFT JOIN guru g ON m.guru_id = g.nip ";

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql += "WHERE m.nama_mapel ILIKE ? OR g.nama_guru ILIKE ?";
        }
        sql += " ORDER BY m.nama_mapel";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mapelDataObservableList.add(new MapelData(
                        rs.getInt("mapel_id"),
                        rs.getInt("kkm_mapel"),
                        rs.getString("nama_mapel"),
                        rs.getString("nip"),
                        rs.getString("nama_guru")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data mata pelajaran.");
            e.printStackTrace();
        }
    }

    private void populateFields(MapelData mapel) {
        mapelField.setText(mapel.getNamaMapel());
        kkmField.setText(String.valueOf(mapel.getKkm()));
        guruField.setText(mapel.getNamaGuru() != null ? mapel.getNamaGuru() : "");
    }

    @FXML
    void onSearchClick(ActionEvent event) {
        loadMapelData(searchField.getText());
    }

    @FXML
    void onRefreshClick(ActionEvent event) {
        searchField.clear();
        clearInputFields();
        loadMapelData("");
    }

    @FXML
    void onAddClick(ActionEvent event) {
        String namaMapel = mapelField.getText();
        String kkmStr = kkmField.getText();
        String namaGuru = guruField.getText();

        if (namaMapel.trim().isEmpty() || kkmStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Nama Mapel dan KKM harus diisi.");
            return;
        }

        try {
            int kkm = Integer.parseInt(kkmStr);
            String guruNip = findGuruNip(namaGuru); // Cari NIP guru

            String sql = "INSERT INTO mapel (nama_mapel, kkm_mapel, guru_id) VALUES (?, ?, ?)";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, namaMapel);
                pstmt.setInt(2, kkm);
                pstmt.setString(3, guruNip);
                pstmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Mata pelajaran baru berhasil ditambahkan.");
                onRefreshClick(null); // Refresh tampilan
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan data. Error: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "KKM harus berupa angka.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        MapelData selectedMapel = mapelList.getSelectionModel().getSelectedItem();
        if (selectedMapel == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih mapel dari tabel untuk diubah.");
            return;
        }

        String namaMapel = mapelField.getText();
        String kkmStr = kkmField.getText();
        String namaGuru = guruField.getText();

        if (namaMapel.trim().isEmpty() || kkmStr.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Nama Mapel dan KKM harus diisi.");
            return;
        }

        try {
            int kkm = Integer.parseInt(kkmStr);
            String guruNip = findGuruNip(namaGuru);

            String sql = "UPDATE mapel SET nama_mapel = ?, kkm_mapel = ?, guru_id = ? WHERE mapel_id = ?";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, namaMapel);
                pstmt.setInt(2, kkm);
                pstmt.setString(3, guruNip);
                pstmt.setInt(4, selectedMapel.getMapelId());
                pstmt.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data mata pelajaran berhasil diperbarui.");
                onRefreshClick(null);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data. Error: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "KKM harus berupa angka.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    @FXML
    void onRemoveClick(ActionEvent event) {
        MapelData selectedMapel = mapelList.getSelectionModel().getSelectedItem();
        if (selectedMapel == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih mapel dari tabel untuk dihapus.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Anda yakin ingin menghapus mapel '" + selectedMapel.getNamaMapel() + "'?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM mapel WHERE mapel_id = ?";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, selectedMapel.getMapelId());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data mapel berhasil dihapus.");
                onRefreshClick(null);

            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data. Mungkin data ini terhubung dengan data lain.");
                e.printStackTrace();
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
            showAlert(Alert.AlertType.ERROR, "UI Error", "Gagal kembali ke halaman admin.");
        }
    }

    private String findGuruNip(String namaGuru) throws Exception {
        if (namaGuru == null || namaGuru.trim().isEmpty()) {
            return null; // Guru tidak di-set (diperbolehkan)
        }

        List<String> nips = new ArrayList<>();
        String sql = "SELECT nip FROM guru WHERE nama_guru = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, namaGuru.trim());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                nips.add(rs.getString("nip"));
            }
        } catch (SQLException e) {
            throw new Exception("Error saat mencari data guru.");
        }

        if (nips.isEmpty()) {
            throw new Exception("Guru dengan nama '" + namaGuru + "' tidak ditemukan.");
        }
        if (nips.size() > 1) {
            throw new Exception("Ditemukan lebih dari satu guru dengan nama '" + namaGuru + "'. Harap gunakan nama yang unik.");
        }
        return nips.get(0);
    }

    private void clearInputFields() {
        mapelList.getSelectionModel().clearSelection();
        mapelField.clear();
        guruField.clear();
        kkmField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner class untuk menampung data di TableView
    public static class MapelData {
        private final int mapelId, kkm;
        private final String namaMapel, guruNip, namaGuru;

        public MapelData(int mapelId, int kkm, String namaMapel, String guruNip, String namaGuru) {
            this.mapelId = mapelId;
            this.kkm = kkm;
            this.namaMapel = namaMapel;
            this.guruNip = guruNip;
            this.namaGuru = namaGuru;
        }

        public int getMapelId() { return mapelId; }
        public int getKkm() { return kkm; }
        public String getNamaMapel() { return namaMapel; }
        public String getGuruNip() { return guruNip; }
        public String getNamaGuru() { return namaGuru; }
    }
}