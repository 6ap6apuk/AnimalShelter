package com.example.shelter;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ShelterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(ShelterApplication.class.getResource("formMain.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
//        String projectPath = System.getProperty("user.dir");
//        String imgPath = projectPath + File.separator + "src" + File.separator +
//                "main" + File.separator + "resources" + File.separator +
//                "images" + File.separator + "logo.jpg";
//        stage.getIcons().add(new Image(imgPath));
//        stage.setTitle("Главное меню");
//        stage.initStyle(StageStyle.UNDECORATED);
//        stage.setResizable(false);
//        stage.setScene(scene);
//        stage.show();

        FXMLLoader fxmlLoader = new FXMLLoader(ShelterApplication.class.getResource("formLogin.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        String projectPath = System.getProperty("user.dir");
        String imgPath = projectPath + File.separator + "src" + File.separator +
                "main" + File.separator + "resources" + File.separator +
                "images" + File.separator + "logo.jpg";
        stage.getIcons().add(new Image(imgPath));
        stage.setTitle("Вход");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}