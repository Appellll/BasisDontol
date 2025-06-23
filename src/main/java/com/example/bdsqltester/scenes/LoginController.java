package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField passwordField;
    @FXML private ChoiceBox<String> selectRole;
    @FXML private TextField usernameField;

    User verifyCredentials(String username, String password, String role) throws SQLException {
        String sql;
        // --- MODIFIKASI: Sekarang kita cek untuk role 'siswa' ---
        if ("siswa".equalsIgnoreCase(role)) {
            sql = "SELECT u.*, s.tahun_ajaran, k.nama_kelas " +
                    "FROM users u " +
                    "JOIN siswa s ON u.username = s.nama_siswa " +
                    "JOIN kelas k ON s.kelas_id = k.kelas_id " +
                    "WHERE u.username = ? AND u.password = ? AND u.role = ?";
        } else {
            sql = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
        }

        try (Connection c = MainDataSource.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs);
                    if ("siswa".equalsIgnoreCase(user.role)) {
                        user.setKelas(rs.getString("nama_kelas"));
                        user.setTahunAjaran(rs.getString("tahun_ajaran"));
                    }
                    return user;
                }
            }
        }
        return null;
    }

    @FXML
    void initialize() {
        // --- MODIFIKASI: Mengubah 'User' menjadi 'Siswa' di ChoiceBox ---
        selectRole.getItems().addAll("Admin", "Siswa", "Guru");
        selectRole.setValue("Siswa"); // Defaultnya sekarang 'Siswa'
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        try {
            User user = verifyCredentials(username, password, role);

            if (user != null) {
                loadSceneForUser(user);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Username, Password, atau Role tidak cocok.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSceneForUser(User user) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader;
        String fxmlFile;

        // --- MODIFIKASI: Menggunakan 'siswa' sebagai case ---
        switch (user.role) {
            case "guru": fxmlFile = "guru-view.fxml"; break;
            case "admin": fxmlFile = "admin-view.fxml"; break;
            default: // Ini akan menangani 'siswa' dan peran lain jika ada
                fxmlFile = "siswa-view.fxml";
                break;
        }

        loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load());

        if ("guru".equals(user.role)) {
            GuruController guruController = loader.getController();
            guruController.setUser(user);
        } else if ("siswa".equals(user.role)) { // <-- MODIFIKASI: Menggunakan 'siswa'
            SiswaController siswaController = loader.getController();
            siswaController.setUser(user);
        }

        app.getPrimaryStage().setScene(scene);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}