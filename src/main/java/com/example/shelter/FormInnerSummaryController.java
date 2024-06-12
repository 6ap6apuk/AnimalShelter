package com.example.shelter;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormInnerSummaryController implements Initializable {

    @FXML
    private Pane paneTop;

    @FXML
    private Button btnCancel;

    @FXML
    private TextField txtCountAll;
    @FXML
    private TextField txtCountNotIll;
    @FXML
    private TextField txtCountVaccinated;
    @FXML
    private TextField txtCountKind;
    @FXML
    private TextField txtCountPrice;
    @FXML
    private TextField txtCountMedicine;
    @FXML
    private TextField txtCountCharity;
    @FXML
    private TextArea txtCountGiven;
    @FXML
    private TextArea txtCountHome;
    @FXML
    private ImageView imgBoxClose;

    @FXML
    private ImageView imgBoxUnshow;

    @FXML
    private Label lblError;

    double mouseX = 0, mouseY = 0;

    ArrayList<Animal> allAnimals = new ArrayList<>();
    ArrayList<String> animalKinds = new ArrayList<>();
    int animalsCount = 0;
    int animalsHealthy = 0;
    int animalsVaccinated = 0;
    int animalsLargestKind = 0;
    int animalsExpenses = 0;
    int animalsExpensesMedicine = 0;
    int animalsCharity = 0;
    StringBuilder animalsGiven = new StringBuilder("");
    StringBuilder animalsHome = new StringBuilder("");


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
        btnCancel.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close();});

        lblError.setText("");
        getData();
        if (animalsCount == 0) {
            lblError.setText("Животных не найдено!");
        }
        else {
            getInfo();
            setSummary();
        }
    }

    public void getData() {
        try {
            if (animalsHome.length() != 0)
                animalsHome.delete(0, animalsHome.length());
            if (animalsGiven.length() != 0)
                animalsGiven.delete(0, animalsGiven.length());

            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // создаем выражение, которое отражает реальное представление данных в базе данных
            // позволяющее создавать новые записи
            String selectAnimal = "SELECT name, age, kind, breed, color_fur, is_ill, " +
                    "description, is_vaccinated, img, gender " +
                    "FROM public.animals;";
            PreparedStatement st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            ResultSet rs = st.executeQuery();
            int countRes = 0;
            while(rs.next()) {
                Animal animal = new Animal(rs.getString(1), rs.getInt(2), rs.getInt(3),
                        rs.getString(4), rs.getString(5), rs.getBoolean(6),
                        rs.getString(7), rs.getBoolean(8),
                        rs.getString(9), rs.getBoolean(10));
                allAnimals.add(animal);
                countRes++;
            }
            animalsCount = countRes;
            // получаем результат
            System.out.println("Записей получено: " + countRes);

            // получение семейств животных
            selectAnimal = "SELECT family " +
                    "FROM public.animal_kinds;";
            st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            rs = st.executeQuery();
            countRes = 0;
            while(rs.next()) {
                animalKinds.add(rs.getString(1));
                countRes++;
            }
            // получаем результат
            System.out.println("Записей видов получено: " + countRes);

            // количество принятых животных
            selectAnimal = "SELECT name, kind, gender " +
                    "FROM public.animals WHERE is_brought = true;";
            st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            rs = st.executeQuery();
            countRes = 0;
            while(rs.next()) {
                animalsGiven.append(rs.getString(1));
                animalsGiven.append("(");
                animalsGiven.append(animalKinds.get(rs.getInt(2) - 1));
                animalsGiven.append(", ");
                if (rs.getBoolean(3))
                    animalsGiven.append(" самец)");
                else
                    animalsGiven.append(" самка)");
                animalsGiven.append(", ");
                countRes++;
            }
            if (countRes != 0)
                animalsGiven.delete(animalsGiven.lastIndexOf(","), animalsGiven.lastIndexOf(" "));
            // получаем результат
            System.out.println("Записей принятых животных получено: " + countRes);

            // количество отданных животных заводчикам
            selectAnimal = "SELECT name, kind, gender " +
                    "FROM public.animals_given;";
            st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            rs = st.executeQuery();
            countRes = 0;
            while(rs.next()) {
                animalsHome.append(rs.getString(1));
                animalsHome.append("(");
                animalsHome.append(animalKinds.get(rs.getInt(2) - 1));
                animalsHome.append(", ");
                if (rs.getBoolean(3))
                    animalsHome.append(" самец)");
                else
                    animalsHome.append(" самка)");
                animalsHome.append(", ");
                countRes++;
            }
            if (countRes != 0)
                animalsHome.delete(animalsHome.lastIndexOf(","), animalsHome.lastIndexOf(" "));

            // получаем результат
            System.out.println("Записей отданных животных получено: " + countRes);

            // количество пожертвований
            selectAnimal = "SELECT SUM(sum_charity) " +
                    "FROM public.charity;";
            st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            rs = st.executeQuery();
            while(rs.next()) {
                animalsCharity = rs.getInt(1);
            }

            // количество затрат на еду
            selectAnimal = "SELECT SUM(sum_kind) " +
                    "FROM public.expenses;";
            st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            rs = st.executeQuery();
            while(rs.next()) {
                animalsExpenses = rs.getInt(1);
            }

            // количество затрат на медицину
            selectAnimal = "SELECT SUM(sum) " +
                    "FROM public.medicine_stat;";
            st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            rs = st.executeQuery();
            while(rs.next()) {
                animalsExpensesMedicine = rs.getInt(1);
            }

            st.close(); // закрываем выражение
            connection.close(); // закрываем подключение
        }
        catch (Exception e) {
            lblError.setText("Ошибка доступа к БД!");
            System.err.println("Ошибка доступа к БД!");
            e.printStackTrace();
        }
    }

    public void getInfo() {
        animalsHealthy = 0;
        animalsVaccinated = 0;
        int[] biggestKinds = new int[animalKinds.size()];
        for (int i = 0; i < animalsCount; i++) {
            Animal thisAnimal = allAnimals.get(i);
            if (!thisAnimal.getIll())
                animalsHealthy++;
            if (thisAnimal.getVaccinated())
                animalsVaccinated++;
            // Проверяем, существует ли элемент в массиве для данного типа животного
            if (thisAnimal.getKind() > 0 && thisAnimal.getKind() <= animalKinds.size()) {
                biggestKinds[thisAnimal.getKind() - 1]++;
            }
        }
        int maxAnimals = biggestKinds[0];
        animalsLargestKind = 0;
        for (int i = 0; i < biggestKinds.length; i++) {
            if (maxAnimals < biggestKinds[i])
            {
                maxAnimals = biggestKinds[i];
                animalsLargestKind = i;
            }
        }
    }

    public void setSummary() {
        txtCountAll.setText(animalsCount + "");
        txtCountNotIll.setText(animalsHealthy + "");
        txtCountVaccinated.setText(animalsVaccinated + "");
        txtCountKind.setText(animalKinds.get(animalsLargestKind));
        txtCountPrice.setText(animalsExpenses + "");
        txtCountMedicine.setText(animalsExpensesMedicine + "");
        txtCountGiven.setText(animalsGiven.toString());
        txtCountHome.setText(animalsHome.toString());
        txtCountCharity.setText(animalsCharity + "");

        txtCountAll.setEditable(false);
        txtCountNotIll.setEditable(false);
        txtCountVaccinated.setEditable(false);
        txtCountKind.setEditable(false);
        txtCountPrice.setEditable(false);
        txtCountMedicine.setEditable(false);
        txtCountGiven.setEditable(false);
        txtCountHome.setEditable(false);
        txtCountCharity.setEditable(false);
    }
}