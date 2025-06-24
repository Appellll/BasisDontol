package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.SppSiswa;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

public class SppSiswaController implements SiswaDataReceiver {

    @FXML private TableView<SppSiswa> sppTable;
    @FXML private TableColumn<SppSiswa, String> bulanColumn;
    @FXML private TableColumn<SppSiswa, String> tagihanColumn;
    @FXML private TableColumn<SppSiswa, String> statusColumn;
    @FXML private Label namaSiswaLabel;
    @FXML private Label infoBeasiswaLabel;

    private User currentUser;
    private final ObservableList<SppSiswa> sppList = FXCollections.observableArrayList();
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    public void setUser(User user) {
        this.currentUser = user;
        namaSiswaLabel.setText("Tagihan SPP untuk: " + currentUser.getUsername());
        loadSppStatus();
    }

    @FXML
    public void initialize() {
        sppTable.setItems(sppList);
        bulanColumn.setCellValueFactory(cell -> cell.getValue().bulanProperty());
        tagihanColumn.setCellValueFactory(cell -> cell.getValue().tagihanProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
    }

    private void loadSppStatus() {
        sppList.clear();
        String sql = "SELECT " +
                "s.tingkat_kelas, s.beasiswa_akademis_nonakademis, s.beasiswa_keringanan, " +
                "spp.nominal_spp " +
                "FROM siswa s " +
                "JOIN spp ON s.tingkat_kelas = spp.tingkat_kelas " +
                "WHERE s.nis = ?";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, currentUser.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double sppAsli = rs.getDouble("nominal_spp");
                boolean dapatBeasiswaAkademis = rs.getBoolean("beasiswa_akademis_nonakademis");
                boolean dapatBeasiswaKeringanan = rs.getBoolean("beasiswa_keringanan");

                double sppAkhir = calculateFinalSpp(conn, sppAsli, dapatBeasiswaAkademis, dapatBeasiswaKeringanan);

                String infoBeasiswaText = getBeasiswaInfo(conn, dapatBeasiswaAkademis, dapatBeasiswaKeringanan);
                infoBeasiswaLabel.setText(infoBeasiswaText);

                // Asumsi SPP untuk 6 bulan ke depan (satu semester)
                String[] semesterBulan = {"Juli", "Agustus", "September", "Oktober", "November", "Desember"};
                for (String bulan : semesterBulan) {
                    sppList.add(new SppSiswa(bulan, currencyFormatter.format(sppAkhir), "Belum Lunas"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double calculateFinalSpp(Connection conn, double sppAsli, boolean beasiswaAkademis, boolean beasiswaKeringanan) throws SQLException {
        double sppPotongan = sppAsli;

        if (beasiswaAkademis) {
            sppPotongan = applyBeasiswa(conn, sppPotongan, "Akademis");
        }
        if (beasiswaKeringanan) {
            sppPotongan = applyBeasiswa(conn, sppPotongan, "Keringanan");
        }
        // Tambahkan logika untuk beasiswa non-akademis jika ada kolomnya

        return sppPotongan > 0 ? sppPotongan : 0;
    }

    private double applyBeasiswa(Connection conn, double currentSpp, String tipeBeasiswa) throws SQLException {
        String sql = "SELECT jumlah_potongan, jenis_potongan FROM beasiswa WHERE tipe_beasiswa = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipeBeasiswa);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int jumlahPotongan = rs.getInt("jumlah_potongan");
                String jenisPotongan = rs.getString("jenis_potongan");

                if ("PERSEN".equalsIgnoreCase(jenisPotongan)) {
                    return currentSpp - (currentSpp * jumlahPotongan / 100.0);
                } else { // NOMINAL
                    return currentSpp - jumlahPotongan;
                }
            }
        }
        return currentSpp;
    }

    private String getBeasiswaInfo(Connection conn, boolean beasiswaAkademis, boolean beasiswaKeringanan) throws SQLException {
        StringBuilder info = new StringBuilder("Status Beasiswa: ");
        if (!beasiswaAkademis && !beasiswaKeringanan) {
            return info.append("Tidak ada").toString();
        }
        if (beasiswaAkademis) info.append("Akademis, ");
        if (beasiswaKeringanan) info.append("Keringanan, ");

        return info.substring(0, info.length() - 2); // Hapus koma terakhir
    }

    @FXML
    void onBackClick(ActionEvent event) {
        SiswaControllerUtil.backToSiswaDashboard(currentUser);
    }
}