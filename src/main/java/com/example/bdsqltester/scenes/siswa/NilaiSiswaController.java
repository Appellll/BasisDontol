package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.NilaiSiswa;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NilaiSiswaController implements SiswaDataReceiver {

    @FXML private TableView<NilaiSiswa> nilaiTable;
    @FXML private TableColumn<NilaiSiswa, String> mapelColumn;
    @FXML private TableColumn<NilaiSiswa, String> tipeColumn;
    @FXML private TableColumn<NilaiSiswa, Integer> nilaiColumn;

    private User currentUser;
    private final ObservableList<NilaiSiswa> nilaiList = FXCollections.observableArrayList();

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        loadNilai();
    }

    @FXML
    public void initialize() {
        nilaiTable.setItems(nilaiList);
        mapelColumn.setCellValueFactory(cell -> cell.getValue().mapelProperty());
        tipeColumn.setCellValueFactory(cell -> cell.getValue().tipeProperty());
        nilaiColumn.setCellValueFactory(cell -> cell.getValue().nilaiProperty().asObject());
    }

    private void loadNilai() {
        String sql = "SELECT m.nama_mapel, n.tipe_nilai, n.nilai_siswa " +
                "FROM nilai n " +
                "JOIN mapel m ON n.mapel_id = m.mapel_id " +
                "WHERE n.siswa_id = ?";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUser.getId()); // NIS siswa
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                nilaiList.add(new NilaiSiswa(
                        rs.getString("nama_mapel"),
                        rs.getString("tipe_nilai"),
                        rs.getInt("nilai_siswa")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onBackClick(ActionEvent event) {
        SiswaControllerUtil.backToSiswaDashboard(currentUser);
    }
}