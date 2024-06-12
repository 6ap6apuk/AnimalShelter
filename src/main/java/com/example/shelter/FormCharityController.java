package com.example.shelter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormCharityController implements Initializable {

    @FXML
    private Pane paneTop;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSend;

    @FXML
    private TextField txtCharity;
    @FXML
    private ImageView imgBoxClose;

    @FXML
    private ImageView imgBoxUnshow;

    @FXML
    private Label lblError;

    double mouseX = 0, mouseY = 0;
    boolean charitySent = false;

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
        btnSend.setOnAction(event -> {
            sendCharity();
            if (charitySent)
                btnCancel.fire();
        } );
        btnCancel.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close();});

        lblError.setText("");
    }

    public void sendCharity() {
        if (!txtCharity.getText().equals("")) {
            try {
                int sumCharity = Integer.parseInt(txtCharity.getText());
                Class.forName("org.postgresql.Driver"); // используемый класс
                String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

                Properties authorization = new Properties(); // свойства подключения к бд
                authorization.setProperty("user", "dbuser1"); // логин
                authorization.setProperty("password", "dbuser1"); // пароль
                Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
                // создаем выражение, которое отражает реальное представление данных в базе данных
                // позволяющее создавать новые записи
                String insertCharity = "INSERT INTO charity (sum_charity)" +
                        "VALUES (?);";
                PreparedStatement st = connection.prepareStatement(insertCharity);
                // заполняем необходимые значения в подготовленном запросе
                st.setInt(1, sumCharity);
                int result = st.executeUpdate();
                // получаем результат
                System.out.println("Запись в charity успешно добавлена. Изменено строк: " + result);
                charitySent = true;

                st.close(); // закрываем выражение
                connection.close(); // закрываем подключение
            } catch (Exception e) {
                lblError.setText("Ошибка доступа к БД!");
                System.err.println("Ошибка доступа к БД!");
                e.printStackTrace();
            }
        }
        else
            lblError.setText("Введите сумму пожертвования.");
    }
}