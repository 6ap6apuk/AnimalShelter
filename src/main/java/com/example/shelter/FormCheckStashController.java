package com.example.shelter;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormCheckStashController implements Initializable {
    @FXML
    private Pane paneTop;
    @FXML
    private Label lblError;
    @FXML
    private Button btnExit;
    @FXML
    private ImageView imgBoxClose;
    @FXML
    private ImageView imgBoxUnshow;
    @FXML
    private TableView<Medicine> tblView;
    @FXML
    private TableColumn<Medicine, String> colMedicine;
    @FXML
    private TableColumn<Medicine, String> colType;
    @FXML
    private TableColumn<Medicine, Double> colPrice;
    @FXML
    private TableColumn<Medicine, Double> colWeight;
    @FXML
    private TableColumn<Medicine, String> colMeasurement;
    @FXML
    private TableColumn<Medicine, Integer> colCountStash;
    double mouseX = 0, mouseY = 0;
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

        // заготовка для вывода возможных ошибок
        lblError.setText("");

        // обработчики кнопок
        btnExit.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close(); });

        getData();
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
            String selectMedicine = "SELECT medicine, type, price, weight, measurement, count_stash " +
                    "FROM public.medicine;";
            PreparedStatement st = connection.prepareStatement(selectMedicine);
            // выполняем подготовленный запрос
            ResultSet rs = st.executeQuery();
            int countRes = 0;
            ObservableList<Medicine> medicines = FXCollections.observableArrayList();
            while(rs.next()) {
                Medicine thisMed = new Medicine(rs.getString(1), rs.getString(2),
                rs.getDouble(3), rs.getInt(4),
                rs.getString(5), rs.getInt(6) );
                medicines.add(thisMed);
                countRes++;
            }
            // получаем результат
            System.out.println("Записей медицины получено: " + countRes);

            // Настраиваем столбцы таблицы
            // вывод названия препарата
            colMedicine.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Medicine, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Medicine, String> p) {
                    Medicine medicine = p.getValue();
                    if (medicine != null) {
                        return new SimpleStringProperty(medicine.getName());
                    } else {
                        return null;
                    }
                }
            });

            // вывод типа препарата
            colType.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Medicine, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Medicine, String> p) {
                    Medicine medicine = p.getValue();
                    if (medicine != null) {
                        return new SimpleStringProperty(medicine.getType());
                    } else {
                        return null;
                    }
                }
            });

            // вывод стоимости препарата
            colPrice.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Medicine, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<Medicine, Double> p) {
                    Medicine medicine = p.getValue();
                    if (medicine != null) {
                        return new SimpleDoubleProperty(medicine.getPrice()).asObject();
                    } else {
                        return null;
                    }
                }
            });

            // вывод веса упаковки препарата
            colWeight.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Medicine, Double>, ObservableValue<Double>>() {
                @Override
                public ObservableValue<Double> call(TableColumn.CellDataFeatures<Medicine, Double> p) {
                    Medicine medicine = p.getValue();
                    if (medicine != null) {
                        return new SimpleDoubleProperty(medicine.getWeight()).asObject();
                    } else {
                        return null;
                    }
                }
            });

            // установка измерений препарата на упаковке
            colMeasurement.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Medicine, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Medicine, String> p) {
                    Medicine medicine = p.getValue();
                    if (medicine != null) {
                        return new SimpleStringProperty(medicine.getMeasurement());
                    } else {
                        return null;
                    }
                }
            });

            // установка количества препаратов
            colCountStash.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Medicine, Integer>, ObservableValue<Integer>>() {
                @Override
                public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Medicine, Integer> p) {
                    Medicine medicine = p.getValue();
                    if (medicine != null) {
                        return new SimpleIntegerProperty(medicine.getCountStash()).asObject();
                    } else {
                        return null;
                    }
                }
            });

            // Привязываем данные к таблице
            tblView.setItems(medicines);
            tblView.setEditable(false);

            st.close(); // закрываем выражение
            connection.close(); // закрываем подключение
        }
        catch (Exception e) {
            lblError.setText("Ошибка доступа к БД!");
            System.err.println("Ошибка доступа к БД!");
            e.printStackTrace();
        }
    }

    private class Medicine {
        private String medicine;
        private String type;
        private double price;
        private double weight;
        private String measurement;
        private int countStash;

        Medicine(String Medicine_name, String Medicine_type, double Medicine_price,
            double Medicine_weight, String Medicine_measurement, int Medicine_count) {
            medicine = Medicine_name;
            type = Medicine_type;
            price = Medicine_price;
            weight = Medicine_weight;
            measurement = Medicine_measurement;
            countStash = Medicine_count;
        }

        public int getCountStash() { return countStash; }
        public String getName() { return medicine; }
        public String getType() { return type; }
        public String getMeasurement() { return measurement; }
        public double getPrice() { return price; }
        public double getWeight() { return weight; }
    }

}