package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.dtos.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

// Kelas ini berisi fungsi bantuan yang dipakai berulang
public class SiswaControllerUtil {
    public static void backToSiswaDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
            Scene scene = new Scene(loader.load());
            SiswaController controller = loader.getController();
            controller.setUser(user);
            HelloApplication.getApplicationInstance().getPrimaryStage().setScene(scene);
            HelloApplication.getApplicationInstance().getPrimaryStage().setTitle("Siswa Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}