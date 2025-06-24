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
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class InputSppController {

    // Komponen FXML
    @FXML private TableView<SppData> sppList;
    @FXML private TableColumn<SppData, String> tingkatList;
    @FXML private TableColumn<SppData, Integer> jumlahList;

    @FXML private TableView<BeasiswaData> beasiswaList;
    @FXML private TableColumn<BeasiswaData, String> beasiswaTypeList;
    @FXML private TableColumn<BeasiswaData, String> potonganList;

    @FXML private ChoiceBox<Integer> tingkatBox;
    @FXML private TextField jumlahField;

    @FXML private ChoiceBox<String> beasiswaBox;
    @FXML private TextField potonganField;

    // ObservableLists untuk menampung data
    private final ObservableList<SppData> sppDataObservableList = FXCollections.observableArrayList();
    private final ObservableList<BeasiswaData> beasiswaDataObservableList = FXCollections.observableArrayList();

    // Formatter untuk angka nominal
    private final NumberFormat numberFormat = NumberFormat.getInstance(new Locale("id", "ID"));

    @FXML
    public void initialize() {
        setupTables();
        setupFormControls();
        loadAllData();

        sppList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) populateSppForm(newSelection);
                }
        );

        beasiswaList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) populateBeasiswaForm(newSelection);
                }
        );
    }

    private void setupTables() {
        // Tabel SPP per Tingkat
        tingkatList.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getTingkatKelas())));
        jumlahList.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getJumlahSpp()).asObject());
        sppList.setItems(sppDataObservableList);

        jumlahList.setCellFactory(tc -> new TableCell<SppData, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : numberFormat.format(item));
            }
        });

        // Tabel Beasiswa
        beasiswaTypeList.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipeBeasiswa()));
        potonganList.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPotonganFormatted())); // Menggunakan string untuk menampilkan %
        beasiswaList.setItems(beasiswaDataObservableList);
    }

    private void setupFormControls() {
        tingkatBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        // Mengisi beasiswaBox dengan opsi yang diminta
        beasiswaBox.setItems(FXCollections.observableArrayList("Akademis", "Non-Akademis", "Keringanan"));
    }

    private void loadAllData() {
        loadSppData();
        loadBeasiswaData();
    }

    private void loadSppData() {
        sppDataObservableList.clear();
        String sql = "SELECT spp_id, tingkat_kelas, nominal_spp FROM spp ORDER BY tingkat_kelas";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                sppDataObservableList.add(new SppData(
                        rs.getInt("spp_id"),
                        rs.getInt("tingkat_kelas"),
                        rs.getInt("nominal_spp")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data SPP.");
            e.printStackTrace();
        }
    }

    private void loadBeasiswaData() {
        beasiswaDataObservableList.clear();
        String sql = "SELECT beasiswa_id, tipe_beasiswa, jumlah_potongan, jenis_potongan FROM beasiswa ORDER BY tipe_beasiswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                beasiswaDataObservableList.add(new BeasiswaData(
                        rs.getInt("beasiswa_id"),
                        rs.getString("tipe_beasiswa"),
                        rs.getInt("jumlah_potongan"),
                        rs.getString("jenis_potongan")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data Beasiswa.");
            e.printStackTrace();
        }
    }

    private void populateSppForm(SppData spp) {
        tingkatBox.setValue(spp.getTingkatKelas());
        jumlahField.setText(numberFormat.format(spp.getJumlahSpp()));
    }

    private void populateBeasiswaForm(BeasiswaData beasiswa) {
        beasiswaBox.setValue(beasiswa.getTipeBeasiswa());
        potonganField.setText(beasiswa.getPotonganFormatted().replace("%", ""));
    }

    private int parsePotonganValue(String input) {
        return Integer.parseInt(input.replaceAll("[.%]", ""));
    }

    @FXML
    void onAddBeasiswaClick(ActionEvent event) {
        if (beasiswaBox.getValue() == null || potonganField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Tipe Beasiswa dan Jumlah Potongan harus diisi.");
            return;
        }
        try {
            String inputPotongan = potonganField.getText().trim();
            String tipe = beasiswaBox.getValue();

            int nilaiPotongan = parsePotonganValue(inputPotongan);
            String jenisPotongan = inputPotongan.contains("%") ? "PERSEN" : "NOMINAL";

            String sql = "INSERT INTO beasiswa (tipe_beasiswa, jumlah_potongan, jenis_potongan) VALUES (?, ?, ?)";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tipe);
                pstmt.setInt(2, nilaiPotongan);
                pstmt.setString(3, jenisPotongan);
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jenis beasiswa baru berhasil ditambahkan.");
                loadAllData();
                clearBeasiswaForm();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan beasiswa. Tipe beasiswa mungkin sudah ada.");
                e.printStackTrace();
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "Jumlah Potongan harus berupa angka atau persentase yang valid.");
        }
    }

    // Metode lainnya (onAddSppClick, onSaveClick, onDeleteClick, etc.) tetap sama seperti sebelumnya...

    @FXML
    void onAddSppClick(ActionEvent event) {
        if (tingkatBox.getValue() == null || jumlahField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Tingkat Kelas dan Jumlah SPP harus diisi.");
            return;
        }

        try {
            String cleanJumlah = jumlahField.getText().replaceAll("\\.", "");
            int jumlah = Integer.parseInt(cleanJumlah);
            int tingkat = tingkatBox.getValue();

            String sql = "INSERT INTO spp (tingkat_kelas, nominal_spp) VALUES (?, ?)";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, tingkat);
                pstmt.setInt(2, jumlah);
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data SPP baru berhasil ditambahkan.");
                loadAllData();
                clearSppForm();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan data. Tingkat kelas mungkin sudah ada.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "Jumlah SPP harus berupa angka yang valid.");
        }
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        SppData selectedSpp = sppList.getSelectionModel().getSelectedItem();
        BeasiswaData selectedBeasiswa = beasiswaList.getSelectionModel().getSelectedItem();

        if (selectedSpp != null && !jumlahField.getText().trim().isEmpty()) {
            try {
                String cleanJumlah = jumlahField.getText().replaceAll("\\.", "");
                int jumlah = Integer.parseInt(cleanJumlah);

                String sql = "UPDATE spp SET nominal_spp = ? WHERE spp_id = ?";
                try (Connection conn = MainDataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, jumlah);
                    pstmt.setInt(2, selectedSpp.getSppId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data SPP berhasil diperbarui.");
                    loadAllData();
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Salah", "Jumlah SPP harus berupa angka yang valid.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data SPP.");
            }
        } else if (selectedBeasiswa != null && !potonganField.getText().trim().isEmpty()) {
            try {
                String inputPotongan = potonganField.getText().trim();
                int nilaiPotongan = parsePotonganValue(inputPotongan);
                String jenisPotongan = inputPotongan.contains("%") ? "PERSEN" : "NOMINAL";

                String sql = "UPDATE beasiswa SET jumlah_potongan = ?, jenis_potongan = ? WHERE beasiswa_id = ?";
                try (Connection conn = MainDataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, nilaiPotongan);
                    pstmt.setString(2, jenisPotongan);
                    pstmt.setInt(3, selectedBeasiswa.getBeasiswaId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data Beasiswa berhasil diperbarui.");
                    loadAllData();
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Salah", "Jumlah Potongan harus berupa angka atau persentase yang valid.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data beasiswa.");
                e.printStackTrace();
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih item dari salah satu tabel dan isi field yang sesuai untuk disimpan.");
        }
    }

    @FXML
    void onDeleteClick(ActionEvent event) {
        SppData selectedSpp = sppList.getSelectionModel().getSelectedItem();
        BeasiswaData selectedBeasiswa = beasiswaList.getSelectionModel().getSelectedItem();

        if (selectedSpp != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus SPP untuk Tingkat " + selectedSpp.getTingkatKelas() + "?", ButtonType.YES, ButtonType.NO);
            if (confirmation.showAndWait().filter(ButtonType.YES::equals).isPresent()) {
                String sql = "DELETE FROM spp WHERE spp_id = ?";
                try (Connection conn = MainDataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedSpp.getSppId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data SPP berhasil dihapus.");
                    onRefreshClick(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data SPP.");
                }
            }
        } else if (selectedBeasiswa != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus beasiswa '" + selectedBeasiswa.getTipeBeasiswa() + "'?", ButtonType.YES, ButtonType.NO);
            if (confirmation.showAndWait().filter(ButtonType.YES::equals).isPresent()) {
                String sql = "DELETE FROM beasiswa WHERE beasiswa_id = ?";
                try (Connection conn = MainDataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, selectedBeasiswa.getBeasiswaId());
                    pstmt.executeUpdate();
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data beasiswa berhasil dihapus.");
                    onRefreshClick(null);
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data beasiswa.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih item dari salah satu tabel untuk dihapus.");
        }
    }

    @FXML
    void onRefreshClick(ActionEvent event) {
        loadAllData();
        clearSppForm();
        clearBeasiswaForm();
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

    private void clearSppForm() {
        sppList.getSelectionModel().clearSelection();
        tingkatBox.setValue(null);
        jumlahField.clear();
    }

    private void clearBeasiswaForm() {
        beasiswaList.getSelectionModel().clearSelection();
        beasiswaBox.setValue(null);
        potonganField.clear();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class SppData {
        private final int sppId;
        private final int tingkatKelas;
        private final int jumlahSpp;

        public SppData(int sppId, int tingkatKelas, int jumlahSpp) {
            this.sppId = sppId;
            this.tingkatKelas = tingkatKelas;
            this.jumlahSpp = jumlahSpp;
        }

        public int getSppId() { return sppId; }
        public int getTingkatKelas() { return tingkatKelas; }
        public int getJumlahSpp() { return jumlahSpp; }
    }

    public static class BeasiswaData {
        private final int beasiswaId;
        private final String tipeBeasiswa;
        private final int jumlahPotongan;
        private final String jenisPotongan;

        public BeasiswaData(int beasiswaId, String tipeBeasiswa, int jumlahPotongan, String jenisPotongan) {
            this.beasiswaId = beasiswaId;
            this.tipeBeasiswa = tipeBeasiswa;
            this.jumlahPotongan = jumlahPotongan;
            this.jenisPotongan = jenisPotongan;
        }

        public int getBeasiswaId() { return beasiswaId; }
        public String getTipeBeasiswa() { return tipeBeasiswa; }
        public int getJumlahPotongan() { return jumlahPotongan; }
        public String getJenisPotongan() { return jenisPotongan; }

        public String getPotonganFormatted() {
            NumberFormat nf = NumberFormat.getInstance(new Locale("id", "ID"));
            if ("PERSEN".equals(jenisPotongan)) {
                return jumlahPotongan + "%";
            }
            return nf.format(jumlahPotongan);
        }
    }
}