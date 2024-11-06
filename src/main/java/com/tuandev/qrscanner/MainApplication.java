package com.tuandev.qrscanner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("QR Scanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("images/qr-code-scan.png"))));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
