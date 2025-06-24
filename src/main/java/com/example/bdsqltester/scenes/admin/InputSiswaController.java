package com.example.bdsqltester.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
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
import java.util.Optional;

public class InputSiswaController {

    // Komponen FXML
    @FXML private TableView<Siswa> siswaTable;
    @FXML private TableColumn<Siswa, String> nisList;
    @FXML private TableColumn<Siswa, String> namaList;
    @FXML private TableColumn<Siswa, String> tahunList;
    @FXML private TableColumn<Siswa, Integer> tingkatList;
    @FXML private TableColumn<Siswa, String> semesterList; // Pastikan fx:id ini ada di FXML
    @FXML private TableColumn<Siswa, Boolean> akademisList;
    @FXML private TableColumn<Siswa, Boolean> keringananList;

    @FXML private TextField namaField;
    @FXML private TextField nisField;
    @FXML private ChoiceBox<Integer> tingkatBox;
    @FXML private ChoiceBox<String> semesterBox;
    @FXML private TextField tahunField;
    @FXML private ChoiceBox<String> akademisBox;
    @FXML private ChoiceBox<String> keringananBox;

    private final ObservableList<Siswa> siswaDataList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadSiswaData();
    }

    private void setupTable() {
        nisList.setCellValueFactory(cellData -> cellData.getValue().nisProperty());
        namaList.setCellValueFactory(cellData -> cellData.getValue().namaSiswaProperty());
        tahunList.setCellValueFactory(cellData -> cellData.getValue().tahunAjaranProperty());
        tingkatList.setCellValueFactory(cellData -> cellData.getValue().tingkatKelasProperty().asObject());
        semesterList.setCellValueFactory(cellData -> cellData.getValue().semesterProperty());

        akademisList.setCellValueFactory(cellData -> cellData.getValue().beasiswaAkademisProperty());
        akademisList.setCellFactory(col -> createBooleanCell("Ya", "Tidak"));

        keringananList.setCellValueFactory(cellData -> cellData.getValue().beasiswaKeringananProperty());
        keringananList.setCellFactory(col -> createBooleanCell("Ya", "Tidak"));

        siswaTable.setItems(siswaDataList);

        siswaTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateForm(newSelection);
                    }
                });
    }

    private void setupForm() {
        tingkatBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5, 6));
        semesterBox.setItems(FXCollections.observableArrayList("Ganjil", "Genap"));
        akademisBox.setItems(FXCollections.observableArrayList("Ya", "Tidak"));
        keringananBox.setItems(FXCollections.observableArrayList("Ya", "Tidak"));
    }

    private void loadSiswaData() {
        siswaDataList.clear();
        String sql = "SELECT nis, nama_siswa, tahun_ajaran, tingkat_kelas, semester, beasiswa_akademis_nonakademis, beasiswa_keringanan FROM siswa ORDER BY nama_siswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                siswaDataList.add(new Siswa(
                        rs.getString("nis"),
                        rs.getString("nama_siswa"),
                        rs.getString("tahun_ajaran"),
                        rs.getInt("tingkat_kelas"),
                        rs.getString("semester"),
                        rs.getBoolean("beasiswa_akademis_nonakademis"),
                        rs.getBoolean("beasiswa_keringanan")
                ));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa.");
            e.printStackTrace();
        }
    }

    private void populateForm(Siswa siswa) {
        nisField.setText(siswa.getNis());
        namaField.setText(siswa.getNamaSiswa());
        tahunField.setText(siswa.getTahunAjaran());
        tingkatBox.setValue(siswa.getTingkatKelas());
        semesterBox.setValue(siswa.getSemester());
        akademisBox.setValue(siswa.isBeasiswaAkademis() ? "Ya" : "Tidak");
        keringananBox.setValue(siswa.isBeasiswaKeringanan() ? "Ya" : "Tidak");
    }

    @FXML
    void onAddClick(ActionEvent event) {
        if (!isFormValid()) return;

        String sql = "INSERT INTO siswa (nis, nama_siswa, tahun_ajaran, tingkat_kelas, semester, beasiswa_akademis_nonakademis, beasiswa_keringanan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nisField.getText());
            pstmt.setString(2, namaField.getText());
            pstmt.setString(3, tahunField.getText());
            pstmt.setInt(4, tingkatBox.getValue());
            pstmt.setString(5, semesterBox.getValue());
            pstmt.setBoolean(6, "Ya".equals(akademisBox.getValue()));
            pstmt.setBoolean(7, "Ya".equals(keringananBox.getValue()));

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Siswa baru berhasil ditambahkan.");
            loadSiswaData();
            clearForm();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menambahkan siswa. Pastikan NIS unik.");
            e.printStackTrace();
        }
    }

    @FXML
    void onSaveClick(ActionEvent event) {
        Siswa selectedSiswa = siswaTable.getSelectionModel().getSelectedItem();
        if (selectedSiswa == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih siswa dari tabel untuk diubah.");
            return;
        }
        if (!isFormValid()) return;

        String sql = "UPDATE siswa SET nama_siswa = ?, tahun_ajaran = ?, tingkat_kelas = ?, semester = ?, beasiswa_akademis_nonakademis = ?, beasiswa_keringanan = ? WHERE nis = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, namaField.getText());
            pstmt.setString(2, tahunField.getText());
            pstmt.setInt(3, tingkatBox.getValue());
            pstmt.setString(4, semesterBox.getValue());
            pstmt.setBoolean(5, "Ya".equals(akademisBox.getValue()));
            pstmt.setBoolean(6, "Ya".equals(keringananBox.getValue()));
            pstmt.setString(7, nisField.getText());

            pstmt.executeUpdate();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data siswa berhasil diperbarui.");
            loadSiswaData();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memperbarui data siswa.");
            e.printStackTrace();
        }
    }

    @FXML
    void onRemoveClick(ActionEvent event) {
        Siswa selectedSiswa = siswaTable.getSelectionModel().getSelectedItem();
        if (selectedSiswa == null) {
            showAlert(Alert.AlertType.WARNING, "Pilihan Kosong", "Pilih siswa dari tabel untuk dihapus.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Anda yakin ingin menghapus data " + selectedSiswa.getNamaSiswa() + "?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String sql = "DELETE FROM siswa WHERE nis = ?";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, selectedSiswa.getNis());
                pstmt.executeUpdate();
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data siswa berhasil dihapus.");
                loadSiswaData();
                clearForm();

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
            e.printStackTrace();
        }
    }

    private void clearForm() {
        siswaTable.getSelectionModel().clearSelection();
        nisField.clear();
        namaField.clear();
        tahunField.clear();
        tingkatBox.setValue(null);
        semesterBox.setValue(null);
        akademisBox.setValue(null);
        keringananBox.setValue(null);
    }

    private boolean isFormValid() {
        if (nisField.getText().trim().isEmpty() || namaField.getText().trim().isEmpty() ||
                tahunField.getText().trim().isEmpty() || tingkatBox.getValue() == null ||
                semesterBox.getValue() == null || akademisBox.getValue() == null ||
                keringananBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Semua field harus diisi.");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private <T> TableCell<T, Boolean> createBooleanCell(String trueText, String falseText) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? trueText : falseText));
            }
        };
    }

    // Inner class untuk menampung data siswa dengan JavaFX Properties
    public static class Siswa {
        private final SimpleStringProperty nis;
        private final SimpleStringProperty namaSiswa;
        private final SimpleStringProperty tahunAjaran;
        private final SimpleIntegerProperty tingkatKelas;
        private final SimpleStringProperty semester;
        private final SimpleBooleanProperty beasiswaAkademis;
        private final SimpleBooleanProperty beasiswaKeringanan;

        public Siswa(String nis, String nama, String tahun, int tingkat, String semester, boolean beasiswaAkademis, boolean beasiswaKeringanan) {
            this.nis = new SimpleStringProperty(nis);
            this.namaSiswa = new SimpleStringProperty(nama);
            this.tahunAjaran = new SimpleStringProperty(tahun);
            this.tingkatKelas = new SimpleIntegerProperty(tingkat);
            this.semester = new SimpleStringProperty(semester);
            this.beasiswaAkademis = new SimpleBooleanProperty(beasiswaAkademis);
            this.beasiswaKeringanan = new SimpleBooleanProperty(beasiswaKeringanan);
        }

        // Getters dan Property Getters
        public String getNis() { return nis.get(); }
        public SimpleStringProperty nisProperty() { return nis; }
        public String getNamaSiswa() { return namaSiswa.get(); }
        public SimpleStringProperty namaSiswaProperty() { return namaSiswa; }
        public String getTahunAjaran() { return tahunAjaran.get(); }
        public SimpleStringProperty tahunAjaranProperty() { return tahunAjaran; }
        public int getTingkatKelas() { return tingkatKelas.get(); }
        public SimpleIntegerProperty tingkatKelasProperty() { return tingkatKelas; }
        public String getSemester() { return semester.get(); }
        public SimpleStringProperty semesterProperty() { return semester; }
        public boolean isBeasiswaAkademis() { return beasiswaAkademis.get(); }
        public SimpleBooleanProperty beasiswaAkademisProperty() { return beasiswaAkademis; }
        public boolean isBeasiswaKeringanan() { return beasiswaKeringanan.get(); }
        public SimpleBooleanProperty beasiswaKeringananProperty() { return beasiswaKeringanan; }
    }
}