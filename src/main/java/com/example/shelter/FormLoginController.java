package com.example.shelter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormLoginController implements Initializable {

    @FXML
    private Pane paneTop;

    @FXML
    private TextField txtLogin;

    @FXML
    private PasswordField txtPass;

    @FXML
    private Label lblError;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnCancel;

    @FXML
    private ImageView imgBoxClose;

    @FXML
    private ImageView imgBoxUnshow;

    double mouseX = 0, mouseY = 0;
    ResourceBundle myResourceBundle;
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // заголовок
        String projectPath = System.getProperty("user.dir");
        String imgPathCross = projectPath + File.separator + "src" + File.separator +
                "main" + File.separator + "resources" + File.separator +
                "images" + File.separator + "cross.png";
        Image imgCross = new Image(imgPathCross);
        imgBoxClose.setImage(imgCross);
        imgBoxClose.setFitWidth(35);
        imgBoxClose.setFitHeight(30);
        imgBoxClose.setPreserveRatio(true);

        String imgPathUnder = projectPath + File.separator + "src" + File.separator +
                "main" + File.separator + "resources" + File.separator +
                "images" + File.separator + "underline.png";
        Image imgUnder = new Image(imgPathUnder);
        imgBoxUnshow.setImage(imgUnder);
        imgBoxUnshow.setFitWidth(25);
        imgBoxUnshow.setFitHeight(30);
        imgBoxUnshow.setPreserveRatio(true);

        // движение окна
        paneTop.setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });
        paneTop.setOnMouseDragged(e -> {
            Stage stage = (Stage) paneTop.getScene().getWindow();
            stage.setX(e.getScreenX() - mouseX);
            stage.setY(e.getScreenY() - mouseY);
        });

        // закрытие окна
        imgBoxClose.setOnMouseClicked(e -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close();} );

        // сворачивание окна
        imgBoxUnshow.setOnMouseClicked(e -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.setIconified(true);} );

        // обработчики кнопок
        btnLogin.setOnAction(event -> getLogin());
        btnCancel.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close();});

        // ограничение введенных символов
        txtLogin.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtLogin.getText().length() > 10) {
                txtLogin.setText(txtLogin.getText().substring(0, 10));
            }
        });

        txtPass.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtPass.getText().length() > 15) {
                txtPass.setText(txtPass.getText().substring(0, 15));
            }
        });

        myResourceBundle = resourceBundle;
        lblError.setText("");
    }

    public void getLogin() {
        String thisRole = "";
        String thisLogin = MD5Hash.hash(txtLogin.getText());
        String thisPass = MD5Hash.hash(txtPass.getText());
        try{
            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // создаем выражение, которое отражает реальное представление данных в базе данных
            // и позволяет не только обновлять выбранные записи, но и создавать новые
            // Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String selectUser = "SELECT role FROM users WHERE login = ? AND password = ?;";
            PreparedStatement st = connection.prepareStatement(selectUser);
            // заполняем необходимые значения в подготовленном запросе
            st.setString(1, thisLogin);
            st.setString(2, thisPass);
            // выполняем подготовленный запрос
            ResultSet table = st.executeQuery();
            // получаем результат

            if(table.next()) {
                thisRole = table.getString(1);
                Parent root;
                switch(thisRole) {
                    case "manager":
                        // форма менеджера
                        try {
                            Stage thisStage = (Stage) btnCancel.getScene().getWindow();
                            thisStage.close();
                            root = FXMLLoader.load(ShelterApplication.class.getResource("formMain.fxml"), myResourceBundle);
                            Stage stage = new Stage();
                            String projectPath = System.getProperty("user.dir");
                            String imgPath = projectPath + File.separator + "src" + File.separator +
                                    "main" + File.separator + "resources" + File.separator +
                                    "images" + File.separator + "logo.jpg";
                            stage.getIcons().add(new Image(imgPath));
                            stage.setTitle("Главное меню");
                            stage.initStyle(StageStyle.UNDECORATED);
                            stage.setResizable(false);
                            stage.setScene(new Scene(root, 1024, 768));
                            stage.show();
                        } catch (IOException e) {
                            lblError.setText("Невозможно открыть форму!");
                            e.printStackTrace();
                        }
                        break;

                    case "guest":
                        // форма пользователя
                        try {
                            Stage thisStage = (Stage) btnCancel.getScene().getWindow();
                            thisStage.close();
                            root = FXMLLoader.load(ShelterApplication.class.getResource("formGuest.fxml"), myResourceBundle);
                            Stage stage = new Stage();
                            String projectPath = System.getProperty("user.dir");
                            String imgPath = projectPath + File.separator + "src" + File.separator +
                                    "main" + File.separator + "resources" + File.separator +
                                    "images" + File.separator + "logo.jpg";
                            stage.getIcons().add(new Image(imgPath));
                            stage.setTitle("Гостевое меню");
                            stage.initStyle(StageStyle.UNDECORATED);
                            stage.setResizable(false);
                            stage.setScene(new Scene(root, 1024, 768));
                            stage.show();
                        } catch (IOException e) {
                            lblError.setText("Невозможно открыть форму!");
                            e.printStackTrace();
                        }
                        break;

                    case "doctor":
                        // форма доктора
                        try {
                            Stage thisStage = (Stage) btnCancel.getScene().getWindow();
                            thisStage.close();
                            root = FXMLLoader.load(ShelterApplication.class.getResource("formDoctor.fxml"), myResourceBundle);
                            Stage stage = new Stage();
                            String projectPath = System.getProperty("user.dir");
                            String imgPath = projectPath + File.separator + "src" + File.separator +
                                    "main" + File.separator + "resources" + File.separator +
                                    "images" + File.separator + "logo.jpg";
                            stage.getIcons().add(new Image(imgPath));
                            stage.setTitle("Медицинское меню");
                            stage.initStyle(StageStyle.UNDECORATED);
                            stage.setResizable(false);
                            stage.setScene(new Scene(root, 1024, 768));
                            stage.show();
                        } catch (IOException e) {
                            lblError.setText("Невозможно открыть форму!");
                            e.printStackTrace();
                        }
                        break;
                }
            }
            else {
                lblError.setText("Ошибка входа! Пользователь не опознан!");
            }

            table.close(); // закрываем результирующий набор
            st.close(); // закрываем выражение
            connection.close(); // закрываем подключение
        }
        catch (Exception e){
            System.err.println("Ошибка доступа к БД!");
            lblError.setText("Ошибка входа!");
            e.printStackTrace();
        }
    }

    public class MD5Hash {
        private static final MessageDigest md5;

        static {
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        public static String hash(String input) {
            try {
                byte[] bytes = md5.digest(input.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : bytes) {
                    sb.append(String.format("%02x", b & 0xff));
                }
                return sb.toString().toLowerCase();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}