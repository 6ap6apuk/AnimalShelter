package com.example.shelter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.*;

public class FormTakeAnimalFIOController implements Initializable, UpdateObserver {

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
                takeAnimal();
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

    public void takeAnimal() {
        int thisId = currentAnimal.getDbId();
        int thisKind = currentAnimal.getKind();
        String thisName = txtName.getText();
        String thisSurname = txtSurname.getText();
        String thisFatherName = txtFatherName.getText();
        String thisAddress = txtAddress.getText();
        // Создаем новый объект Calendar для получения текущего дня
        Calendar calendar = new GregorianCalendar();

        // Устанавливаем время в 00:00:00, чтобы получить только дату
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Преобразуем календарь в java.sql.Date
        java.sql.Date currentDay = new java.sql.Date(calendar.getTime().getTime());

        try {
            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // создаем выражение, удаляющие запись животного
            String deleteAnimal = "DELETE FROM public.animals " +
                    " WHERE id = ?";
            PreparedStatement st = connection.prepareStatement(deleteAnimal);
            st.setInt(1, thisId);
            // выполняем подготовленный запрос
            int result = st.executeUpdate();
            // получаем результат
            System.out.println("Записей удалено: " + result);
            st.close(); // закрываем выражение

            // создаем выражение, которое обновляет записи в таблице затрат
            deleteAnimal = "UPDATE expenses SET animal_count = animal_count - 1 " +
                    " WHERE id_kind = ?;";
            st = connection.prepareStatement(deleteAnimal);
            // заполняем необходимые значения в подготовленном запрос
            st.setInt(1, thisKind);
            result = st.executeUpdate();
            System.out.println("Запись в expenses добавлена. Изменено строк: " + result);

            // создаем выражение, которое обновляет записи в таблице затрат
            deleteAnimal = "UPDATE expenses SET sum_kind = animal_count * eat_per_month * money_per_food " +
                    " WHERE id_kind = ?;";
            st = connection.prepareStatement(deleteAnimal);
            // заполняем необходимые значения в подготовленном запрос
            st.setInt(1, thisKind);
            result = st.executeUpdate();
            System.out.println("Запись в expenses добавлена. Изменено строк: " + result);

            int thisIdName;
            deleteAnimal = "SELECT id FROM animal_persons WHERE " +
                    "name = ? AND " +
                    "surname = ? AND " +
                    "father_name = ?;";
            st = connection.prepareStatement(deleteAnimal);
            // заполняем необходимые значения в подготовленном запрос
            st.setString(1, thisName);
            st.setString(2, thisSurname);
            st.setString(3, thisFatherName);
            ResultSet rs = st.executeQuery();

            // проверка на наличие человека
            if (!rs.next()) {
                System.out.println("Нет данных");

                // заносим нового посетителя
                deleteAnimal = "INSERT INTO animal_persons (name, surname, father_name) " +
                        "VALUES (?, ?, ?);";
                st = connection.prepareStatement(deleteAnimal);
                // заполняем необходимые значения в подготовленном запрос
                st.setString(1, thisName);
                st.setString(2, thisSurname);
                st.setString(3, thisFatherName);
                result = st.executeUpdate();
                System.out.println("Запись в animal_persons добавлена. Изменено строк: " + result);

                // получение идентификатора добавленной записи
                deleteAnimal = "SELECT id FROM animal_persons WHERE " +
                        "name = ? AND " +
                        "surname = ? AND" +
                        "father_name = ?;";
                st = connection.prepareStatement(deleteAnimal);
                // заполняем необходимые значения в подготовленном запрос
                st.setString(1, thisName);
                st.setString(2, thisSurname);
                st.setString(3, thisFatherName);
                rs = st.executeQuery();
            }
            // получение идентификатора человека
            thisIdName = rs.getInt(1);
            // создаем выражение для занесения данных в таблицу животных, которых забрали
            deleteAnimal = "INSERT INTO animals_given " +
                    "(name, gender, age, kind, address, " +
                    "date_given, is_vaccinated, id_person)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            st = connection.prepareStatement(deleteAnimal);
            // заполняем необходимые значения в подготовленном запрос
            st.setString(1, currentAnimal.getName());
            st.setBoolean(2, currentAnimal.getGender());
            st.setInt(3, currentAnimal.getAge());
            st.setInt(4, currentAnimal.getKind());
            st.setString(5, thisAddress);
            st.setDate(6, currentDay);
            st.setBoolean(7, currentAnimal.getVaccinated());
            st.setInt(8, thisIdName);
            result = st.executeUpdate();
            System.out.println("Запись в animals_given добавлена. Изменено строк: " + result);

            st.close(); // закрываем выражение
            connection.close(); // закрываем подключение

            animalSent = true;
        }
        catch (Exception e) {
            lblError.setText("Ошибка доступа к БД!");
            System.err.println("Ошибка доступа к БД!");
            e.printStackTrace();
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