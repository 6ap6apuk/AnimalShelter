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
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class FormTakeAnimalController implements Initializable, UpdateObserver {

    private UpdateObserver myObserver;

    public void setObserver(UpdateObserver observer) {
        myObserver = observer;
    }

    @FXML
    private Pane paneTop;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtAge;

    @FXML
    private TextField txtBreed;

    @FXML
    private TextField txtColor;

    @FXML
    private TextField txtId;

    @FXML
    private TextArea txtDesc;
    @FXML
    private ComboBox cmbFamily;
    @FXML
    private CheckBox chbVaccinated;
    @FXML
    private CheckBox chbIllness;
    @FXML
    private CheckBox chbGender;
    @FXML
    private Button btnTake;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnLeft;
    @FXML
    private Button btnRight;
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

        // отключение редактирования у полей
        txtId.setEditable(false);
        txtAge.setEditable(false);
        txtDesc.setEditable(false);
        txtBreed.setEditable(false);
        txtColor.setEditable(false);
        txtName.setEditable(false);
        cmbFamily.setDisable(true);
        chbIllness.setDisable(true);
        chbVaccinated.setDisable(true);
        chbGender.setDisable(true);

        // обработчики кнопок
        btnTake.setOnAction(event -> {
            openTakeFIOForm(Integer.parseInt(txtId.getText()));
        } );
        btnLeft.setOnAction(event -> {
            if (Integer.parseInt(txtId.getText()) > 1) {
                txtId.setText(Integer.parseInt(txtId.getText()) - 1 + "");
                getInfo(Integer.parseInt(txtId.getText()));
                btnRight.setDisable(false);
            }
            if (Integer.parseInt(txtId.getText()) == 1) {
                btnLeft.setDisable(true);
            }
        });
        btnRight.setOnAction(event -> {
            if (Integer.parseInt(txtId.getText()) < animalsCount) {
                txtId.setText(Integer.parseInt(txtId.getText()) + 1 + "");
                getInfo(Integer.parseInt(txtId.getText()));
                btnLeft.setDisable(false);
            }
            if (Integer.parseInt(txtId.getText()) == animalsCount) {
                btnRight.setDisable(true);
            }
        });
        btnCancel.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close();});

        getData();
        ObservableList<String> animFams = FXCollections.observableArrayList(animalKinds);
        cmbFamily.setItems(animFams);
        if (animalsCount != 0) {
            getInfo(1);
            btnLeft.setDisable(true);
            if (animalsCount == 1)
                btnRight.setDisable(true);
        }
        else
            lblError.setText("Животных не найдено!");
    }

    @FXML
    private void openTakeFIOForm(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("formTakeAnimalFIO.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            String projectPath = System.getProperty("user.dir");
            String imgPath = projectPath + File.separator + "src" + File.separator +
                    "main" + File.separator + "resources" + File.separator +
                    "images" + File.separator + "logo.jpg";
            stage.getIcons().add(new Image(imgPath));
            stage.setTitle("Ввод ФИО");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

            // Передаем экземпляр главной формы в FormAddController
            FormTakeAnimalFIOController controller = (FormTakeAnimalFIOController) loader.getController();
            controller.initData(allAnimals.get(id - 1));
            controller.setObserver(this);
        } catch (IOException e) {
            lblError.setText("Невозможно открыть форму!");
            e.printStackTrace();
        }
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
            // очищаем поля
        }
        catch (Exception e) {
            lblError.setText("Ошибка доступа к БД!");
            System.err.println("Ошибка доступа к БД!");
            e.printStackTrace();
        }
    }

    public void getInfo(int id) {
        // получаем текущее животное
        Animal thisAnimal = allAnimals.get(id - 1);

        // выводим информацию о нём
        txtId.setText(id + "");
        txtAge.setText(thisAnimal.getAge() + "");
        txtDesc.setText(thisAnimal.getDescription());
        txtBreed.setText(thisAnimal.getBreed());
        txtColor.setText(thisAnimal.getColor());
        txtName.setText(thisAnimal.getName());
        cmbFamily.setValue(cmbFamily.getItems().get(thisAnimal.getKind() - 1));
        chbIllness.setSelected(thisAnimal.getIll());
        chbVaccinated.setSelected(thisAnimal.getVaccinated());
        chbGender.setSelected(thisAnimal.getGender());
    }

    @Override
    public void update() {
        System.out.println("Животное успешно забрали!");
        myObserver.update();
        btnCancel.fire();
    }

    public FormTakeAnimalController() {
        // Подписываемся на изменение свойства updateRequired
        updateRequired.addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                update();
                // Сбрасываем флаг после обновления
                updateRequired.set(false);
            }
        });
    }
}