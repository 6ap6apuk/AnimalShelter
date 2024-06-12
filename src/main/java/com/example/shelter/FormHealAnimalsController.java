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
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import static com.example.shelter.UpdateObserver.updateRequired;

public class FormHealAnimalsController implements Initializable, UpdateObserver {

    @FXML
    private Pane paneTop;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtAge;

    @FXML
    private TextField txtId;

    @FXML
    private TextArea txtHeal;

    @FXML
    private ComboBox cmbFamily;

    @FXML
    private CheckBox chbVaccinated;

    @FXML
    private CheckBox chbIllness;

    @FXML
    private Button btnHeal;

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
        txtHeal.setEditable(false);
        txtName.setEditable(false);
        cmbFamily.setDisable(true);
        chbIllness.setDisable(true);
        chbVaccinated.setDisable(true);

        // обработчики кнопок
        btnHeal.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                openChooseHealForm(Integer.parseInt(txtId.getText()));
            }
        });

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
    private void openChooseHealForm(int id) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("formChooseHeal.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            String projectPath = System.getProperty("user.dir");
            String imgPath = projectPath + File.separator + "src" + File.separator +
                    "main" + File.separator + "resources" + File.separator +
                    "images" + File.separator + "logo.jpg";
            stage.getIcons().add(new Image(imgPath));
            stage.setTitle("Выбор лекарства");
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

            // Передаем экземпляр главной формы в FormAddController
            FormChooseHealController controller = (FormChooseHealController) loader.getController();
            controller.initData(allAnimals.get(id - 1));
            controller.setObserver(this);
        } catch (IOException e) {
            lblError.setText("Невозможно открыть форму!");
            e.printStackTrace();
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
            // создаем выражение, которое отражает реальное представление данных в базе данных
            // позволяющее создавать новые записи
            String selectAnimal = "SELECT name, age, kind, breed, color_fur, is_ill, " +
                    "description, is_vaccinated, img, id, gender " +
                    "FROM public.animals " +
                    "WHERE is_ill = true;";
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

            for (Animal animal : allAnimals) {
                String sql = "SELECT m.medicine FROM animal_treatment at JOIN medicine m ON at.id_medicine = m.id_medicine WHERE at.id_animal = ? AND end_date > NOW()";
                st = connection.prepareStatement(sql);
                st.setInt(1, animal.getDbId());
                rs = st.executeQuery();

                StringBuilder medicine = new StringBuilder();
                while (rs.next()) {
                    medicine.append(rs.getString("medicine"));
                    medicine.append(", ");
                }
                if (!medicine.isEmpty())
                    medicine.delete(medicine.lastIndexOf(","), medicine.lastIndexOf(" "));
                animal.setDescription(medicine.toString());
            }

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
        txtHeal.setText(thisAnimal.getDescription());
        txtName.setText(thisAnimal.getName());
        cmbFamily.setValue(cmbFamily.getItems().get(thisAnimal.getKind() - 1));
        chbIllness.setSelected(thisAnimal.getIll());
        chbVaccinated.setSelected(thisAnimal.getVaccinated());
    }

    @Override
    public void update() {
        lblError.setText("");
        // получение информации из классов
        getData();
        getInfo(Integer.parseInt(txtId.getText()));
        System.out.println("Произошло обновление!");
    }

    public FormHealAnimalsController() {
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