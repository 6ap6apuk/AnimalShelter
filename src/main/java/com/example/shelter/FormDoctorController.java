package com.example.shelter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormDoctorController implements Initializable {
    @FXML
    private Button btnHealAnimals;
    @FXML
    private Button btnCheckStash;
    @FXML
    private Label lblCount;
    @FXML
    public Label lblError;
    @FXML
    public TextField txtSearchAnimal;
    @FXML
    private ImageView imgBoxClose;
    @FXML
    private ImageView imgBoxUnshow;
    @FXML
    private ImageView imgBoxBack;
    @FXML
    public TilePane tilePane;
    @FXML
    private Pane paneTop;

    double mouseX = 0, mouseY = 0;
    ArrayList<Animal> allAnimals = new ArrayList<>();
    ArrayList<String> animalKinds = new ArrayList<>();
    ArrayList<Animal> allAnimalsSorted = new ArrayList<>();
    int animalsCount = 0;
    ResourceBundle myResource;

    @FXML
    public void initialize(URL location, ResourceBundle resources){
        lblCount.setText("Всего: ");
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

        String imgPathBack = projectPath + File.separator + "src" + File.separator +
                "main" + File.separator + "resources" + File.separator +
                "images" + File.separator + "arrow.png";
        Image imgBack = new Image(imgPathBack);
        imgBoxBack.setImage(imgBack);
        imgBoxBack.setFitWidth(35);
        imgBoxBack.setFitHeight(30);
        imgBoxBack.setPreserveRatio(true);

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

        imgBoxBack.setOnMouseClicked(e -> {
            Parent root;
            try {
                Stage thisStage = (Stage) btnHealAnimals.getScene().getWindow();
                thisStage.close();
                root = FXMLLoader.load(ShelterApplication.class.getResource("formLogin.fxml"), resources);
                Stage stage = new Stage();
                String imgPath = projectPath + File.separator + "src" + File.separator +
                        "main" + File.separator + "resources" + File.separator +
                        "images" + File.separator + "logo.jpg";
                stage.getIcons().add(new Image(imgPath));
                stage.setTitle("Вход");
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setResizable(false);
                stage.setScene(new Scene(root, 800, 600));
                stage.show();
            } catch (IOException m) {
                lblError.setText("Невозможно открыть форму!");
                m.printStackTrace();
            }
        } );

        // ограничение введенных символов и работа сортировки
        txtSearchAnimal.textProperty().addListener((observable, oldValue, newValue) -> {
            // ограничение поля на 15 символов
            if (txtSearchAnimal.getText().length() > 15) {
                txtSearchAnimal.setText(txtSearchAnimal.getText().substring(0, 15));
            }
            // получение поискового запроса
            String searchField = txtSearchAnimal.getText().toLowerCase();
            // если поле пустое - отображаем все карточки
            if (searchField.equals("") || newValue.equals(""))
            {
                getCopyAnimals(allAnimals, allAnimalsSorted);
            }
            else
                // если поисковый запрос уменьшается
                if (oldValue.length() > newValue.length())
                {
                    // получаем заново весь массив животных
                    getCopyAnimals(allAnimals, allAnimalsSorted);
                    // удаляем неподходящие элементы
                    for (int i = 0; i < allAnimalsSorted.size(); i++) {
                        Animal thisAnimal = allAnimalsSorted.get(i);
                        // если кличка содержит искомое значение
                        if (!thisAnimal.getName().toLowerCase().contains(searchField)) {
                            allAnimalsSorted.remove(i);
                            i--;
                        }
                    }
                }
                else {
                    // удаляем неподходящие элементы
                    for (int i = 0; i < allAnimalsSorted.size(); i++) {
                        Animal thisAnimal = allAnimalsSorted.get(i);
                        // если кличка содержит искомое значение
                        if (!thisAnimal.getName().toLowerCase().contains(searchField)) {
                            allAnimalsSorted.remove(i);
                            i--;
                        }
                    }
                }
            setInfoBlocks();
        });

        // обработчики кнопок
        btnHealAnimals.setOnAction(new EventHandler<ActionEvent>() {
                                   public void handle(ActionEvent event) {
                                       Parent root;
                                       try {
                                           root = FXMLLoader.load(ShelterApplication.class.getResource("formHealAnimals.fxml"), resources);
                                           Stage stage = new Stage();
                                           String projectPath = System.getProperty("user.dir");
                                           String imgPath = projectPath + File.separator + "src" + File.separator +
                                                   "main" + File.separator + "resources" + File.separator +
                                                   "images" + File.separator + "logo.jpg";
                                           stage.getIcons().add(new Image(imgPath));
                                           stage.setTitle("Лечения животного");
                                           stage.initStyle(StageStyle.UNDECORATED);
                                           stage.setResizable(false);
                                           stage.setScene(new Scene(root, 800, 600));
                                           stage.show();
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                                   }
                               });
                btnCheckStash.setOnAction(new EventHandler<ActionEvent>() {
                                   public void handle(ActionEvent event) {
                                       Parent root;
                                       try {
                                           root = FXMLLoader.load(ShelterApplication.class.getResource("formCheckStash.fxml"), resources);
                                           Stage stage = new Stage();
                                           String projectPath = System.getProperty("user.dir");
                                           String imgPath = projectPath + File.separator + "src" + File.separator +
                                                   "main" + File.separator + "resources" + File.separator +
                                                   "images" + File.separator + "logo.jpg";
                                           stage.getIcons().add(new Image(imgPath));
                                           stage.setTitle("Проверка склада");
                                           stage.initStyle(StageStyle.UNDECORATED);
                                           stage.setResizable(false);
                                           stage.setScene(new Scene(root, 800, 600));
                                           stage.show();
                                       } catch (IOException e) {
                                           e.printStackTrace();
                                       }
                                   }
                               });

        lblError.setText("");
        // получение информации из классов
        getData();
        // передача информации в блоки
        setInfoBlocks();

    }

    public void getData() {
        try {
            allAnimals.clear();
            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // создаем выражение, которое отражает реальное представление данных в базе данных
            // позволяющее создавать новые записи
            String selectAnimal = "SELECT name, age, kind, breed, color_fur, is_ill, " +
                    "description, is_vaccinated, img, id, gender " +
                    "FROM public.animals;";
            PreparedStatement st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            ResultSet rs = st.executeQuery();
            int countRes = 0;
            while(rs.next()) {
                Animal animal = new Animal(rs.getString(1), rs.getInt(2), rs.getInt(3),
                        rs.getString(4), rs.getString(5), rs.getBoolean(6),
                        rs.getString(7), rs.getBoolean(8), rs.getString(9),
                        rs.getInt(10), rs.getBoolean(11));
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

            st.close(); // закрываем выражение
            connection.close(); // закрываем подключение
            lblError.setText("");

            // копируем животных для работы поиска
            getCopyAnimals(allAnimals, allAnimalsSorted);
        }
        catch (Exception e) {
            lblError.setText("Ошибка доступа к БД!");
            System.err.println("Ошибка доступа к БД!");
            e.printStackTrace();
        }
    }

    public void setInfoBlocks() {
        // очищаем область
        tilePane.getChildren().removeAll(tilePane.getChildren());

        tilePane.setPadding(new Insets(10));
        tilePane.setHgap(10);
        tilePane.setVgap(10);

        try {
            for (int i = 0; i < allAnimalsSorted.size(); i++) {
                // получение текущего животного из массива
                Animal thisAnimal = allAnimalsSorted.get(i);
                // установка изображения по его пути
                String projectPath = System.getProperty("user.dir");
                String imgAnimal = projectPath + File.separator + "src" + File.separator +
                        "main" + File.separator + "resources" + File.separator +
                        "images" + File.separator + "animals" + File.separator + thisAnimal.getImage();
                ImageView imageView = new ImageView(new Image(imgAnimal));
                // тень для изображения
                DropShadow ds = new DropShadow( 5, Color.BLACK );
                imageView.setEffect( ds );
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);

                // создание шаблона карточки животного
                StackPane stackPane = new StackPane();
                stackPane.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");
                stackPane.setPrefSize(306, 200);

                GridPane gridPane = new GridPane();
                gridPane.getColumnConstraints().add(new ColumnConstraints(100));

                // деление блока на колонки
                ColumnConstraints column2 = new ColumnConstraints(125, 150, Double.MAX_VALUE);
                column2.setHgrow(Priority.ALWAYS);
                gridPane.getColumnConstraints().add(column2);
                // высота колонок
                gridPane.getRowConstraints().add(new RowConstraints(230));

                String animalCart = "Кличка: " + thisAnimal.getName() + "\n" +
                        "Пол: " + (thisAnimal.getGender() ? "Самец" : "Самка") + "\n" +
                        "Возраст: " + thisAnimal.getAge() + "\n" +
                        "Вид: " + animalKinds.get(thisAnimal.getKind() - 1) + "\n" +
                        "Порода: " + thisAnimal.getBreed() + "\n" +
                        "Окрас: " + thisAnimal.getColor() + "\n" +
                        "Болезнь: " + (thisAnimal.getIll() ? "Да" : "Нет") + "\n" +
                        "Описание: " + thisAnimal.getDescription() + "\n" +
                        "Годовая прививка: " + (thisAnimal.getVaccinated() ? "Да" : "Нет");

                // добавление текста на карточку
                Label nameLabel = new Label(animalCart);
                // перенос строки и максимальная ширина строки
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(160);

                // установка изображения в первую колонку
                gridPane.add(imageView, 0, 0);
                // установка описания во вторую колонку
                gridPane.add(nameLabel, 1, 0);
                // установка отступов
                GridPane.setMargin(nameLabel, new Insets(20));
                GridPane.setMargin(imageView, new Insets(0, 0, 0, 10));

                // добавляем созданную карточку на форму
                stackPane.getChildren().add(gridPane);
                tilePane.getChildren().add(stackPane);
            }
            lblError.setText("");
            lblCount.setText("Количество животных: " + animalsCount);
        }
        catch (Exception e) {
            lblError.setText("Ошибка вывода изображений!");
            e.printStackTrace();
        }
    }

    private void getCopyAnimals(ArrayList<Animal> originalList, ArrayList<Animal> copyList) {
        copyList.clear();
        for (int i = 0; i < originalList.size(); i++) {
            copyList.add(i, originalList.get(i));
        }
    }

}