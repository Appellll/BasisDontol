package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class InputKelasController {

    // FXML Components
    @FXML private ListView<Object> anggotaList;
    @FXML private ListView<Guru> guruList;
    @FXML private ListView<Kelas> kelasList;
    @FXML private ListView<Siswa> siswaList;
    @FXML private TextField kelasField;
    @FXML private TextField waliKelasField;

    // ObservableLists
    private final ObservableList<Guru> guruObservableList = FXCollections.observableArrayList();
    private final ObservableList<Kelas> kelasObservableList = FXCollections.observableArrayList();
    private final ObservableList<Siswa> siswaObservableList = FXCollections.observableArrayList();
    private final ObservableList<Object> anggotaObservableList = FXCollections.observableArrayList();

    private Guru currentWaliKelas = null;

    @FXML
    public void initialize() {
        setupListViews();
        loadAllMasterLists();

        kelasList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateDetailsForKelas(newSelection);
            }
        });
    }

    private void setupListViews() {
        guruList.setItems(guruObservableList);
        kelasList.setItems(kelasObservableList);
        siswaList.setItems(siswaObservableList);
        anggotaList.setItems(anggotaObservableList);

        guruList.setCellFactory(lv -> createCell(guru -> String.format("%s (%s)", guru.getNamaGuru(), guru.getNip())));
        kelasList.setCellFactory(lv -> createCell(Kelas::getNamaKelas));
        siswaList.setCellFactory(lv -> createCell(siswa -> String.format("%s (%s)", siswa.getNamaSiswa(), siswa.getNis())));

        anggotaList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof Guru) {
                    Guru g = (Guru) item;
                    setText(String.format("%s (%s) - WALI KELAS", g.getNamaGuru(), g.getNip()));
                } else if (item instanceof Siswa) {
                    Siswa s = (Siswa) item;
                    setText(String.format("%s (%s)", s.getNamaSiswa(), s.getNis()));
                }
            }
        });
    }

    @FXML
    void onRefreshClick(ActionEvent event) {
        kelasField.clear();
        waliKelasField.clear();
        anggotaObservableList.clear();
        kelasList.getSelectionModel().clearSelection();
        currentWaliKelas = null;
        loadAllMasterLists();
    }

    private void loadAllMasterLists() {
        loadGuruList();
        loadKelasList();
        loadSiswaTanpaKelasList();
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

    private void loadKelasList() {
        kelasObservableList.clear();
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT kelas_id, nama_kelas, guru_id FROM kelas ORDER BY nama_kelas");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                kelasObservableList.add(new Kelas(rs.getInt("kelas_id"), rs.getString("nama_kelas"), rs.getString("guru_id")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat daftar kelas.");
        }
    }

    private void loadSiswaTanpaKelasList() {
        siswaObservableList.clear();
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT nis, nama_siswa FROM siswa WHERE kelas_id IS NULL ORDER BY nama_siswa");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                siswaObservableList.add(new Siswa(rs.getString("nis"), rs.getString("nama_siswa")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat daftar siswa tanpa kelas.");
        }
    }

    private void populateDetailsForKelas(Kelas kelas) {
        kelasField.setText(kelas.getNamaKelas());
        anggotaObservableList.clear();
        currentWaliKelas = null;
        waliKelasField.clear();

        if (kelas.getGuruId() != null) {
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT nip, nama_guru FROM guru WHERE nip = ?")) {
                pstmt.setString(1, kelas.getGuruId());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    currentWaliKelas = new Guru(rs.getString("nip"), rs.getString("nama_guru"));
                    anggotaObservableList.add(currentWaliKelas);
                    waliKelasField.setText(currentWaliKelas.getNamaGuru());
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat wali kelas.");
            }
        }

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT nis, nama_siswa FROM siswa WHERE kelas_id = ? ORDER BY nama_siswa")) {
            pstmt.setInt(1, kelas.getKelasId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                anggotaObservableList.add(new Siswa(rs.getString("nis"), rs.getString("nama_siswa")));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat anggota kelas.");
        }
    }

    @FXML void onAddWaliKelasClick(ActionEvent event) {
        Guru selectedGuru = guruList.getSelectionModel().getSelectedItem();
        if (selectedGuru == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih guru dari 'Daftar Guru' terlebih dahulu.");
            return;
        }
        anggotaObservableList.removeIf(item -> item instanceof Guru);
        anggotaObservableList.add(0, selectedGuru);
        currentWaliKelas = selectedGuru;
        waliKelasField.setText(selectedGuru.getNamaGuru());
    }

    @FXML void onRemoveWaliKelasClick(ActionEvent event) {
        anggotaObservableList.removeIf(item -> item instanceof Guru);
        currentWaliKelas = null;
        waliKelasField.clear();
    }

    @FXML void onAddMuridClick(ActionEvent event) {
        Siswa selectedSiswa = siswaList.getSelectionModel().getSelectedItem();
        if (selectedSiswa != null) {
            anggotaObservableList.add(selectedSiswa);
            siswaObservableList.remove(selectedSiswa);
            sortAnggotaList();
        } else {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih siswa dari 'Daftar Siswa' terlebih dahulu.");
        }
    }

    @FXML void onRemoveMuridClick(ActionEvent event) {
        Object selected = anggotaList.getSelectionModel().getSelectedItem();
        if (selected instanceof Siswa) {
            anggotaObservableList.remove(selected);
            siswaObservableList.add((Siswa) selected);
            siswaObservableList.sort(Comparator.comparing(Siswa::getNamaSiswa));
        } else {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih siswa dari 'Anggota Kelas' untuk dihapus.");
        }
    }

    // --- METODE INI TELAH DIPERBARUI ---
    @FXML
    void onClearAnggotaClick(ActionEvent event) {
        // 1. Buat daftar sementara untuk siswa yang akan dipindahkan kembali
        List<Siswa> siswaToMove = new ArrayList<>();
        for (Object obj : anggotaObservableList) {
            if (obj instanceof Siswa) {
                siswaToMove.add((Siswa) obj);
            }
        }

        // 2. Pindahkan semua siswa yang ada di daftar sementara kembali ke daftar siswa utama
        siswaObservableList.addAll(siswaToMove);
        siswaObservableList.sort(Comparator.comparing(Siswa::getNamaSiswa));

        // 3. Kosongkan daftar anggota kelas sepenuhnya (termasuk guru)
        anggotaObservableList.clear();

        // 4. Reset juga field dan status wali kelas
        waliKelasField.clear();
        currentWaliKelas = null;
    }

    @FXML void onAddKelasClick(ActionEvent event) {
        if (kelasField.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama kelas tidak boleh kosong.");
            return;
        }
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO kelas (nama_kelas) VALUES (?)")) {
            pstmt.setString(1, kelasField.getText().trim());
            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Kelas baru berhasil dibuat.");
            loadKelasList();
            kelasField.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuat kelas baru.");
        }
    }

    @FXML void onSaveClick(ActionEvent event) {
        Kelas selectedKelas = kelasList.getSelectionModel().getSelectedItem();
        if (selectedKelas == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas yang ingin disimpan.");
            return;
        }
        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);
            String oldWaliKelasNip = selectedKelas.getGuruId();
            String newWaliKelasNip = (currentWaliKelas != null) ? currentWaliKelas.getNip() : null;
            if (oldWaliKelasNip != null && !oldWaliKelasNip.equals(newWaliKelasNip)) {
                updateStatusWaliKelas(conn, oldWaliKelasNip, false);
            }
            if (newWaliKelasNip != null) {
                updateStatusWaliKelas(conn, newWaliKelasNip, true);
            }
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE kelas SET nama_kelas = ?, guru_id = ? WHERE kelas_id = ?")) {
                pstmt.setString(1, kelasField.getText().trim());
                pstmt.setString(2, newWaliKelasNip);
                pstmt.setInt(3, selectedKelas.getKelasId());
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE siswa SET kelas_id = NULL WHERE kelas_id = ?")) {
                pstmt.setInt(1, selectedKelas.getKelasId());
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE siswa SET kelas_id = ? WHERE nis = ?")) {
                for (Object obj : anggotaObservableList) {
                    if (obj instanceof Siswa) {
                        pstmt.setInt(1, selectedKelas.getKelasId());
                        pstmt.setString(2, ((Siswa) obj).getNis());
                        pstmt.addBatch();
                    }
                }
                pstmt.executeBatch();
            }
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Semua perubahan berhasil disimpan ke database.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error Transaksi", "Terjadi kesalahan saat menyimpan. Semua perubahan dibatalkan.");
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            onRefreshClick(null);
        }
    }

    private void updateStatusWaliKelas(Connection conn, String nip, boolean status) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement("UPDATE guru SET status_wali_kelas = ? WHERE nip = ?")) {
            pstmt.setBoolean(1, status);
            pstmt.setString(2, nip);
            pstmt.executeUpdate();
        }
    }

    private void sortAnggotaList() {
        FXCollections.sort(anggotaObservableList, (o1, o2) -> {
            if (o1 instanceof Guru) return -1;
            if (o2 instanceof Guru) return 1;
            if (o1 instanceof Siswa && o2 instanceof Siswa) {
                return ((Siswa) o1).getNamaSiswa().compareTo(((Siswa) o2).getNamaSiswa());
            }
            return 0;
        });
    }

    @FXML void onBackClick(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(new Scene(loader.load()));
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Admin Dashboard");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal kembali ke halaman admin.");
        }
    }

    private <T> ListCell<T> createCell(java.util.function.Function<T, String> converter) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : converter.apply(item));
            }
        };
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Inner classes
    public static class Guru {
        private final String nip, namaGuru;
        public Guru(String nip, String nama) { this.nip = nip; this.namaGuru = nama; }
        public String getNip() { return nip; }
        public String getNamaGuru() { return namaGuru; }
    }

    public static class Kelas {
        private final int kelasId;
        private final String namaKelas, guruId;
        public Kelas(int id, String nama, String guruId) { this.kelasId = id; this.namaKelas = nama; this.guruId = guruId; }
        public int getKelasId() { return kelasId; }
        public String getNamaKelas() { return namaKelas; }
        public String getGuruId() { return guruId; }
    }

    public static class Siswa {
        private final String nis, namaSiswa;
        public Siswa(String nis, String nama) { this.nis = nis; this.namaSiswa = nama; }
        public String getNis() { return nis; }
        public String getNamaSiswa() { return namaSiswa; }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; return Objects.equals(nis, ((Siswa) o).nis); }
        @Override public int hashCode() { return Objects.hash(nis); }
    }
}