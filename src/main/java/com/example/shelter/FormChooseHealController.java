package com.example.shelter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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

public class FormChooseHealController implements Initializable, UpdateObserver {

    private UpdateObserver myObserver;
    public void setObserver(UpdateObserver observer) {
        myObserver = observer;
    }

    @FXML
    private Pane paneTop;
    @FXML
    private TextField txtDose;
    @FXML
    private TextField txtFrequency;
    @FXML
    private ComboBox cmbMedicine;
    @FXML
    private DatePicker dateBegin;
    @FXML
    private DatePicker dateEnd;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private ImageView imgBoxClose;
    @FXML
    private ImageView imgBoxUnshow;
    @FXML
    private Label lblError;
    @FXML
    private Label lblMeasurement;
    double mouseX = 0, mouseY = 0;
    boolean animalHealed = false;
    Animal currentAnimal;
    java.sql.Date dateStart;
    java.sql.Date dateFinish;

    ObservableList<Medicine> medicines = FXCollections.observableArrayList();
    ObservableList<String> medString = FXCollections.observableArrayList();

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

        // ограничения ввода для текстовых полей
        txtDose.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtDose.getText().length() > 2) {
                txtDose.setText(txtDose.getText().substring(0, 2));
            }
            if (newValue.matches("[0-9]+$")) {
                int number = Integer.parseInt(newValue);
                if (number <= 20 && number > 0) {
                    return;
                }
                txtDose.setText(oldValue);
            }
        });

        txtFrequency.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtFrequency.getText().length() > 2) {
                txtFrequency.setText(txtFrequency.getText().substring(0, 2));
            }
            if (newValue.matches("[0-9]+$")) {
                int number = Integer.parseInt(newValue);
                if (number <= 20 && number > 0) {
                    return;
                }
                txtFrequency.setText(oldValue);
            }
        });

        // заготовка для вывода возможных ошибок
        lblError.setText("");

        // заготовка для вывода граммовки
        lblMeasurement.setText("");

        // обработчики кнопок
        btnSave.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                healAnimal();
                if (animalHealed) {
                    myObserver.update();
                    btnCancel.fire();
                }
            }
        });

        // Обработчики событий для сохранения выбранных дат
        dateBegin.setOnAction(event -> {
            dateStart = Date.valueOf(dateBegin.getValue());
        });

        dateEnd.setOnAction(event -> {
            dateFinish = Date.valueOf(dateEnd.getValue());
        });

        btnCancel.setOnAction(event -> {
            Stage stage = (Stage) paneTop.getScene().getWindow();
            stage.close();
        });

        getData();
        cmbMedicine.setItems(medString);
        cmbMedicine.setValue(cmbMedicine.getItems().get(0));
        lblMeasurement.setText(medicines.get(0).getMeasurement());

        cmbMedicine.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                // Получаем индекс выбранного элемента
                int selectedIndex = cmbMedicine.getSelectionModel().getSelectedIndex();

                // Проверяем, что индекс не равен -1 (то есть элемент выбран)
                if (selectedIndex != -1) {
                    // Получаем выбранный объект Medicine и обновляем текст подписи
                    lblMeasurement.setText(medicines.get(selectedIndex).getMeasurement());
                }
            }
        });
    }

    public void healAnimal() {
        if (dateStart.before(dateFinish) && !txtDose.getText().equals("") && !txtFrequency.getText().equals("")) {
            int idCmb = cmbMedicine.getSelectionModel().selectedIndexProperty().getValue();
            int thisDose = Integer.parseInt(txtDose.getText());
            int thisFrequency = Integer.parseInt(txtFrequency.getText());
            long diffInMilliseconds = dateFinish.getTime() - dateStart.getTime();
            long diffInDays = diffInMilliseconds / (24  *  60  *  60  *  1000);

            // (мг/день * количество дней) / количество мг в пачке
            int takenPacks = (int) (thisDose * thisFrequency * diffInDays / Math.round(medicines.get(idCmb).getWeight()));
            if (takenPacks < medicines.get(idCmb).getStash()) {
                try {
                    Class.forName("org.postgresql.Driver"); // используемый класс
                    String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

                    Properties authorization = new Properties(); // свойства подключения к бд
                    authorization.setProperty("user", "dbuser1"); // логин
                    authorization.setProperty("password", "dbuser1"); // пароль
                    Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
                    // создаем выражение, удаляющие запись животног

                    // заносим нового посетителя
                    String healAnimal = "INSERT INTO animal_treatment (id_animal, id_medicine, dosage, " +
                            "frequency, start_date, end_date, taken_amount) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?);";
                    PreparedStatement st = connection.prepareStatement(healAnimal);
                    // заполняем необходимые значения в подготовленном запрос
                    st.setInt(1, currentAnimal.getDbId());
                    st.setInt(2, medicines.get(idCmb).getDbId());
                    st.setInt(3, thisDose);
                    st.setInt(4, thisFrequency);
                    st.setDate(5, dateStart);
                    st.setDate(6, dateFinish);
                    st.setInt(7, takenPacks);
                    int result = st.executeUpdate();
                    System.out.println("Запись в animal_treatment добавлена. Изменено строк: " + result);

                    // занесение данных в статистику по медикаментам
                    healAnimal = "SELECT id_used FROM medicine_stat WHERE " +
                            "id_medicine = ?;";
                    st = connection.prepareStatement(healAnimal);
                    // заполняем необходимые значения в подготовленном запрос
                    st.setInt(1, medicines.get(idCmb).getDbId());
                    ResultSet rs = st.executeQuery();

                    // проверка на наличие записи использования такого препарата
                    if (!rs.next()) {
                        System.out.println("Нет данных");

                        // заносим новые данные
                        healAnimal = "INSERT INTO medicine_stat (id_medicine, count_used, sum) " +
                                "VALUES (?, ?, ?);";
                        st = connection.prepareStatement(healAnimal);
                        // заполняем необходимые значения в подготовленном запрос
                        st.setInt(1, medicines.get(idCmb).getDbId());
                        st.setInt(2, takenPacks);
                        st.setDouble(3, medicines.get(idCmb).getPrice() * takenPacks);
                        result = st.executeUpdate();
                        System.out.println("Запись в medicine_stat добавлена. Изменено строк: " + result);
                    }
                    else {
                        healAnimal = "UPDATE medicine_stat SET count_used = count_used + ?," +
                                " sum = sum + ?  WHERE id_medicine = ?;";
                        st = connection.prepareStatement(healAnimal);
                        // заполняем необходимые значения в подготовленном запрос
                        st.setInt(1, takenPacks);
                        st.setDouble(2, medicines.get(idCmb).getPrice() * takenPacks);
                        st.setInt(3, medicines.get(idCmb).getDbId());
                        result = st.executeUpdate();
                        System.out.println("Обновление таблицы medicine_stat. Изменено строк: " + result);
                    }
                    
                    healAnimal = "UPDATE medicine SET count_stash = count_stash - ? WHERE id_medicine = ?;";
                    st = connection.prepareStatement(healAnimal);
                    st.setInt(1, takenPacks);
                    st.setInt(2, medicines.get(idCmb).getDbId());
                    result = st.executeUpdate();
                    System.out.println("Обновление таблицы medicine. Изменено строк: " + result);

                    st.close(); // закрываем выражение
                    connection.close(); // закрываем подключение

                    // все выполнилось верно
                    animalHealed = true;
                } catch (Exception e) {
                    lblError.setText("Ошибка доступа к БД!");
                    System.err.println("Ошибка доступа к БД!");
                    e.printStackTrace();
                }
            } else {
                lblError.setText("Недостаточно упаковок выбранного вещества!");
                System.err.println("Недостаточно упаковок выбранного вещества!");
            }
        }
        else {
            lblError.setText("Неверно введённые данные!");
            System.err.println("Неверно введённые данные!");
        }
    }

    public void getData() {
        try {
            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // получение хранимой медицины
            String selectMedicine = "SELECT medicine, type, price, weight, measurement, count_stash, id_medicine " +
                    "FROM public.medicine;";
            PreparedStatement st = connection.prepareStatement(selectMedicine);
            // выполняем подготовленный запрос
            ResultSet rs = st.executeQuery();
            int countRes = 0;
            while(rs.next()) {
                Medicine thisMed = new Medicine(rs.getString(1), rs.getString(2),
                        rs.getDouble(3), rs.getInt(4),
                        rs.getString(5), rs.getInt(6), rs.getInt(7) );
                medicines.add(thisMed);
                medString.add(thisMed.getName());
                countRes++;
            }
            // получаем результат
            System.out.println("Записей медицины получено: " + countRes);

            st.close(); // закрываем выражение
            connection.close(); // закрываем подключение
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

    private class Medicine {
        private int dbId;
        private String medicine;
        private String type;
        private double price;
        private double weight;
        private String measurement;
        private int countStash;

        Medicine(String Medicine_name, String Medicine_type, double Medicine_price,
                 double Medicine_weight, String Medicine_measurement, int Medicine_count,
                int Medicine_dbId) {
            medicine = Medicine_name;
            type = Medicine_type;
            price = Medicine_price;
            weight = Medicine_weight;
            measurement = Medicine_measurement;
            countStash = Medicine_count;
            dbId = Medicine_dbId;
        }

        int getStash() { return countStash; }
        int getDbId() {return dbId; }
        String getName() { return medicine; }
        String getType() { return type; }
        String getMeasurement() { return measurement; }
        double getPrice() { return price; }
        double getWeight() { return weight; }

    }
}