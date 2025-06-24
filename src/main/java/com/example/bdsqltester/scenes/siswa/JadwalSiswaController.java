package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.JadwalSiswa;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JadwalSiswaController implements SiswaDataReceiver {

    @FXML private TableView<JadwalSiswa> jadwalTable;
    @FXML private TableColumn<JadwalSiswa, String> hariColumn;
    @FXML private TableColumn<JadwalSiswa, String> jamColumn;
    @FXML private TableColumn<JadwalSiswa, String> mapelColumn;
    @FXML private TableColumn<JadwalSiswa, String> guruColumn;

    private User currentUser;
    private final ObservableList<JadwalSiswa> jadwalList = FXCollections.observableArrayList();

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        loadJadwal();
    }

    @FXML
    public void initialize() {
        jadwalTable.setItems(jadwalList);
        hariColumn.setCellValueFactory(cell -> cell.getValue().hariProperty());
        jamColumn.setCellValueFactory(cell -> cell.getValue().jamProperty());
        mapelColumn.setCellValueFactory(cell -> cell.getValue().mapelProperty());
        guruColumn.setCellValueFactory(cell -> cell.getValue().guruProperty());
    }

    private void loadJadwal() {
        String sql = "SELECT j.hari, j.jam_mulai, j.jam_selesai, m.nama_mapel, g.nama_guru " +
                "FROM jadwal j " +
                "JOIN mapel m ON j.mapel_id = m.mapel_id " +
                "JOIN guru g ON j.guru_id = g.nip " +
                "WHERE j.kelas_id = (SELECT kelas_id FROM siswa WHERE nis = ?)";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUser.getId()); // NIS siswa
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String jam = rs.getString("jam_mulai") + " - " + rs.getString("jam_selesai");
                jadwalList.add(new JadwalSiswa(
                        rs.getString("hari"),
                        jam,
                        rs.getString("nama_mapel"),
                        rs.getString("nama_guru")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onBackClick(ActionEvent event) {
        // Logika kembali ke dashboard siswa
        SiswaControllerUtil.backToSiswaDashboard(currentUser);
    }
}