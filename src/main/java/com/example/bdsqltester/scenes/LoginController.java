package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.scenes.admin.AdminController;
import com.example.bdsqltester.scenes.guru.GuruController;
import com.example.bdsqltester.scenes.siswa.SiswaController; // <-- TAMBAHKAN IMPORT INI
import com.example.bdsqltester.scenes.wali_kelas.WaliKelasController;
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

    @FXML
    private TextField passwordField;
    @FXML
    private ChoiceBox<String> selectRole;
    @FXML
    private TextField usernameField;

    @FXML
    void initialize() {
        selectRole.getItems().addAll("Admin", "Siswa", "Guru", "Wali Kelas");
        selectRole.setValue("Siswa");
    }

    private User verifyCredentials(String username, String password, String role) throws SQLException {
        String sql;
        User user = null;

        try (Connection c = MainDataSource.getConnection()) {
            switch (role.toLowerCase()) {
                case "admin":
                    sql = "SELECT username, password FROM admins WHERE username = ? AND password = ?";
                    try (PreparedStatement stmt = c.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            user = new User();
                            user.setUsername(rs.getString("username"));
                            user.setRole("admin");
                        }
                    }
                    break;

                case "siswa":
                    sql = "SELECT nis, nama_siswa, password FROM siswa WHERE nis = ? AND password = ?";
                    try (PreparedStatement stmt = c.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            user = new User();
                            user.setId(rs.getString("nis"));
                            user.setUsername(rs.getString("nama_siswa"));
                            user.setRole("siswa");
                        }
                    }
                    break;

                case "guru":
                    sql = "SELECT nip, nama_guru, password FROM guru WHERE nip = ? AND password = ?";
                    try (PreparedStatement stmt = c.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            user = new User();
                            user.setId(rs.getString("nip"));
                            user.setNip(rs.getString("nip"));
                            user.setUsername(rs.getString("nama_guru"));
                            user.setRole("guru");
                        }
                    }
                    break;

                case "wali kelas":
                    sql = "SELECT nip, nama_guru, password, status_wali_kelas FROM guru WHERE nip = ? AND password = ?";
                    try (PreparedStatement stmt = c.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            if (rs.getBoolean("status_wali_kelas")) {
                                user = new User();
                                user.setId(rs.getString("nip"));
                                user.setNip(rs.getString("nip"));
                                user.setUsername(rs.getString("nama_guru"));
                                user.setRole("wali kelas");
                                user.setWaliKelas(true);
                            }
                        }
                    }
                    break;
            }
        }
        return user;
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Username, password, dan role harus diisi.");
            return;
        }

        try {
            User user = verifyCredentials(username, password, role);

            if (user != null) {
                loadSceneForUser(user);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Kombinasi username, password, dan role tidak valid.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Terjadi kesalahan database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSceneForUser(User user) throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        FXMLLoader loader;
        String fxmlFile;

        // Logika ini mengarahkan ke FXML yang benar berdasarkan role
        if (user.isWaliKelas()) {
            fxmlFile = "wali_kelas-view.fxml";
        } else {
            switch (user.getRole().toLowerCase()) {
                case "guru":
                    fxmlFile = "guru-view.fxml";
                    break;
                case "admin":
                    fxmlFile = "admin-view.fxml";
                    break;
                default: // "siswa"
                    fxmlFile = "siswa-view.fxml";
                    break;
            }
        }

        loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load());

        // Memperbaiki bagian pengiriman data ke SiswaController
        if (user.isWaliKelas()) {
            WaliKelasController waliKelasController = loader.getController();
            waliKelasController.setUser(user);
        } else if ("guru".equals(user.getRole())) {
            GuruController guruController = loader.getController();
            guruController.setUser(user);
        } else if ("admin".equals(user.getRole())) {
            AdminController adminController = loader.getController();
            adminController.setUser(user);
        } else if ("siswa".equals(user.getRole())) {
            // ===== PERUBAHAN DI SINI =====
            // Memanggil metode setUser(user) yang benar, bukan setUserId(int)
            SiswaController siswaController = loader.getController();
            siswaController.setUser(user);
            // ============================
        }

        app.getPrimaryStage().setScene(scene);
        app.getPrimaryStage().setTitle(user.getRole() + " Dashboard");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}