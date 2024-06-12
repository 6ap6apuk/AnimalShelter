package com.example.shelter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormGiveAnimalFIOController implements Initializable, UpdateObserver {

    private UpdateObserver myObserver;
    public void setObserver(UpdateObserver observer) {
        myObserver = observer;
    }

    @FXML
    private Pane paneTop;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtSurname;
    @FXML
    private TextField txtFatherName;
    @FXML
    private TextField txtAddress;

    @FXML
    private Button btnTake;

    @FXML
    private Button btnCancel;

    @FXML
    private ImageView imgBoxClose;

    @FXML
    private ImageView imgBoxUnshow;

    @FXML
    private Label lblError;

    double mouseX = 0, mouseY = 0;
    Animal currentAnimal;
    boolean animalSent = false;

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
        imgBoxClose.setOnMouseClicked(e -> {
            Stage stage = (Stage) paneTop.getScene().getWindow();
            stage.close();
        });

        // сворачивание окна
        imgBoxUnshow.setOnMouseClicked(e -> {
            Stage stage = (Stage) paneTop.getScene().getWindow();
            stage.setIconified(true);
        });

        // заготовка для вывода возможных ошибок
        lblError.setText("");

        // обработчики кнопок
        btnTake.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                giveAnimal();
                if (animalSent) {
                    myObserver.update();
                    btnCancel.fire();
                }
            }
        });

        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) paneTop.getScene().getWindow();
            stage.close();
        });
    }

    public void giveAnimal() {
        String thisName = txtName.getText();
        String thisSurname = txtSurname.getText();
        String thisFatherName = txtFatherName.getText();
        if (!thisSurname.equals("") && !thisName.equals("") && !thisFatherName.equals("")) {
            try {
                Class.forName("org.postgresql.Driver"); // используемый класс
                String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

                Properties authorization = new Properties(); // свойства подключения к бд
                authorization.setProperty("user", "dbuser1"); // логин
                authorization.setProperty("password", "dbuser1"); // пароль
                Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
                // создаем выражение, которое отражает реальное представление данных в базе данных
                // позволяющее создавать новые записи

                int thisIdName;
                String insertAnimal = "SELECT id FROM animal_persons WHERE " +
                        "name = ? AND " +
                        "surname = ? AND " +
                        "father_name = ?;";
                PreparedStatement st = connection.prepareStatement(insertAnimal);
                // заполняем необходимые значения в подготовленном запрос
                st.setString(1, thisName);
                st.setString(2, thisSurname);
                st.setString(3, thisFatherName);
                ResultSet rs = st.executeQuery();

                int result;
                // проверка на наличие человека
                if (!rs.next()) {
                    System.out.println("Нет данных");

                    // заносим нового посетителя
                    insertAnimal = "INSERT INTO animal_persons (name, surname, father_name) " +
                            "VALUES (?, ?, ?);";
                    st = connection.prepareStatement(insertAnimal);
                    // заполняем необходимые значения в подготовленном запрос
                    st.setString(1, thisName);
                    st.setString(2, thisSurname);
                    st.setString(3, thisFatherName);
                    result = st.executeUpdate();
                    System.out.println("Запись в animal_persons добавлена. Изменено строк: " + result);

                    // получение идентификатора добавленной записи
                    insertAnimal = "SELECT id FROM animal_persons WHERE " +
                            "name = ? AND " +
                            "surname = ? AND " +
                            "father_name = ?;";
                    st = connection.prepareStatement(insertAnimal);
                    // заполняем необходимые значения в подготовленном запрос
                    st.setString(1, thisName);
                    st.setString(2, thisSurname);
                    st.setString(3, thisFatherName);
                    rs = st.executeQuery();
                    rs.next();
                }
                // получение идентификатора человека
                thisIdName = rs.getInt(1);

                insertAnimal = "INSERT INTO animals_suggested (name, age, kind, breed, " +
                        "color_fur, is_ill, description, is_vaccinated, img, gender, " +
                        "brought_by) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                st = connection.prepareStatement(insertAnimal);
                // заполняем необходимые значения в подготовленном запросе
                st.setString(1, currentAnimal.getName());
                st.setInt(2, currentAnimal.getAge());
                st.setInt(3, currentAnimal.getKind());
                st.setString(4, currentAnimal.getBreed());
                st.setString(5, currentAnimal.getColor());
                st.setBoolean(6, currentAnimal.getIll());
                st.setString(7, currentAnimal.getDescription());
                st.setBoolean(8, currentAnimal.getVaccinated());
                st.setString(9, currentAnimal.getImage());
                st.setBoolean(10, currentAnimal.getGender());
                st.setInt(11, thisIdName);
                // выполняем подготовленный запрос
                result = st.executeUpdate();
                // получаем результат
                System.out.println("Запись успешно добавлена. Изменено строк: " + result);

                st.close(); // закрываем выражение// создаем выражение, которое отражает реальное представление данных в базе данных
                connection.close(); // закрываем подключение
                // очищаем поля
                lblError.setText("");
                animalSent = true;
            }
            catch (Exception e) {
                lblError.setText("Ошибка доступа к БД или неверные данные!");
                System.err.println("Ошибка доступа к БД или неверные данные!");
                e.printStackTrace();
            }
        }
        else {
            lblError.setText("Неверно задано имя!");
            System.err.println("Неверно задано имя!");
        }
    }

    void initData(Animal animalForTake) {
        currentAnimal = animalForTake;
    }

    public void update() {
        // Уведомляем наблюдателя о том, что произошло обновление
        System.out.println("Информация успешно добавлена. Обновляю главную форму.");
        // Здесь код для обновления главной формы
    }
}