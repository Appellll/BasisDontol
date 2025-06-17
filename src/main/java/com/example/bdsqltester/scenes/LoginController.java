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

    @FXML
    private TextField passwordField;
    @FXML
    private ChoiceBox<String> selectRole;
    @FXML
    private TextField usernameField;

    User verifyCredentials(String username, String password, String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND role = ?";
        try (Connection c = MainDataSource.getConnection();
             PreparedStatement stmt = c.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, role.toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    if (dbPassword.equals(password)) {
                        return new User(rs);
                    }
                }
            }
        }
        return null;
    }

    @FXML
    void initialize() {
        selectRole.getItems().addAll("Admin", "User", "Guru");
        selectRole.setValue("User");
    }

    @FXML
    void onLoginClick(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        try (Connection conn = MainDataSource.getConnection()) {
            User user = verifyCredentials(username, password, role);

            if (user != null) {
                // Jika yang login adalah guru, ambil NIP-nya
                if ("guru".equals(user.role)) {
                    // Asumsi: username di tabel users sama dengan nama_guru di tabel guru
                    String sqlGetNip = "SELECT nip FROM guru WHERE nama_guru = ?";
                    try (PreparedStatement stmtGuru = conn.prepareStatement(sqlGetNip)) {
                        stmtGuru.setString(1, user.getUsername());
                        ResultSet rsGuru = stmtGuru.executeQuery();

                        if (rsGuru.next()) {
                            user.setNip(rsGuru.getString("nip")); // Simpan NIP ke objek User
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Login Gagal", "Akun user ditemukan, tapi tidak terhubung dengan data guru.");
                            return;
                        }
                    }
                }

                loadSceneForUser(user);

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "Kredensial Tidak Valid.");
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

        switch (user.role) {
            case "guru":
                fxmlFile = "guru-view.fxml";
                break;
            case "admin":
                fxmlFile = "admin-view.fxml";
                break;
            default: // "user" atau siswa
                fxmlFile = "siswa-view.fxml";
                break;
        }

        loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
        Scene scene = new Scene(loader.load());

        // Kirim data user ke controller tujuan
        if ("guru".equals(user.role)) {
            GuruController guruController = loader.getController();
            guruController.setUser(user);
        } else if ("user".equals(user.role)) {
            // Contoh jika diperlukan untuk SiswaController
            // SiswaController siswaController = loader.getController();
            // siswaController.setUser(user);
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