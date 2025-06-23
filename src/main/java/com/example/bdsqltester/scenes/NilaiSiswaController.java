package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.NilaiSiswa;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NilaiSiswaController {

    @FXML private Label judulLabel;
    @FXML private TableView<NilaiSiswa> nilaiTable;
    @FXML private TableColumn<NilaiSiswa, String> mapelColumn;
    @FXML private TableColumn<NilaiSiswa, String> tipeColumn;
    @FXML private TableColumn<NilaiSiswa, Number> nilaiColumn;
    @FXML private TableColumn<NilaiSiswa, String> tanggalColumn;
    @FXML private TableColumn<NilaiSiswa, String> guruColumn; // <-- TAMBAHAN BARU
    @FXML private Button backButton;

    private User user;
    private final ObservableList<NilaiSiswa> nilaiList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        mapelColumn.setCellValueFactory(cellData -> cellData.getValue().mataPelajaranProperty());
        tipeColumn.setCellValueFactory(cellData -> cellData.getValue().tipeNilaiProperty());
        nilaiColumn.setCellValueFactory(cellData -> cellData.getValue().nilaiProperty());
        tanggalColumn.setCellValueFactory(cellData -> cellData.getValue().tanggalProperty());
        guruColumn.setCellValueFactory(cellData -> cellData.getValue().namaGuruProperty()); // <-- TAMBAHAN BARU
    }

    public void setUser(User user) {
        this.user = user;
        judulLabel.setText("Daftar Nilai: " + user.getUsername());
        loadNilai();
    }

    private void loadNilai() {
        String findNisSql = "SELECT nis FROM siswa WHERE nama_siswa = ?";
        // --- QUERY SQL DIPERBARUI DENGAN JOIN KE TABEL GURU ---
        String nilaiSql = "SELECT m.nama_mapel, n.tipe_nilai, n.nilai_siswa, n.tanggal_input, g.nama_guru " +
                "FROM nilai n " +
                "JOIN mapel m ON n.mapel_id = m.mapel_id " +
                "JOIN guru g ON n.guru_id = g.nip " + // <-- JOIN BARU
                "WHERE n.siswa_id = ? " +
                "ORDER BY n.tanggal_input DESC";

        try (Connection connection = MainDataSource.getConnection();
             PreparedStatement findNisStmt = connection.prepareStatement(findNisSql)) {

            findNisStmt.setString(1, this.user.getUsername());
            ResultSet rsNis = findNisStmt.executeQuery();

            if (rsNis.next()) {
                String nis = rsNis.getString("nis");
                try (PreparedStatement nilaiStmt = connection.prepareStatement(nilaiSql)) {
                    nilaiStmt.setString(1, nis);
                    ResultSet rsNilai = nilaiStmt.executeQuery();

                    nilaiList.clear();
                    while (rsNilai.next()) {
                        nilaiList.add(new NilaiSiswa(rsNilai));
                    }
                    nilaiTable.setItems(nilaiList);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal mengambil data nilai dari database.");
        }
    }

    @FXML
    void onBackClick(ActionEvent event) throws IOException {
        Stage currentStage = (Stage) backButton.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
        Scene scene = new Scene(loader.load());
        SiswaController siswaController = loader.getController();
        siswaController.setUser(this.user);
        currentStage.setScene(scene);
        currentStage.setTitle("Menu Siswa");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}