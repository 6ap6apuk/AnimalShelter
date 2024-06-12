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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Pattern;

public class FormGiveAnimalController implements Initializable, UpdateObserver {
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
    private TextArea txtDesc;
    @FXML
    private Label lblError;
    @FXML
    private ComboBox cmbFamily;
    @FXML
    private CheckBox chbIllness;
    @FXML
    private CheckBox chbVaccinated;
    @FXML
    private CheckBox chbGender;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnClear;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnPicture;
    @FXML
    private ImageView imgBoxClose;
    @FXML
    private ImageView imgBoxUnshow;
    double mouseX = 0, mouseY = 0;
    String namePicture;
    Animal currentAnimal;
    ArrayList<String> animalKinds = new ArrayList<>();
    boolean isImageAttached = false;

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
        btnAdd.setOnAction(event -> {
            openGiveFIOForm();
        });
        btnClear.setOnAction(event -> clearSpaces());
        btnCancel.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close(); });

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg"));
        btnPicture.setOnAction(event -> {
            isImageAttached = false;
            File selectedFile = fileChooser.showOpenDialog(btnPicture.getScene().getWindow());
            if (selectedFile != null) {
                try {
                    // Сохранение изображения
                    try (FileInputStream fis = new FileInputStream(selectedFile)) {
                        String imgPath = projectPath + File.separator + "src" + File.separator +
                                "main" + File.separator + "resources" + File.separator +
                                "images" + File.separator + "animals";
                        File destFile = new File(imgPath, selectedFile.getName());
                        try (FileOutputStream fos = new FileOutputStream(destFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            isImageAttached = true;
                            namePicture = selectedFile.getName();
                        } catch (IOException e) {
                            lblError.setText("Ошибка сохранения!");
                        }
                    } catch (IOException e) {
                        lblError.setText("Ошибка загрузки!");
                    }
                } catch (Exception e) {
                    lblError.setText("Изображение не загружено!");
                }
            }
        });

        String cyrillicRegex = "^[А-Яа-яЁё]+$";
        Pattern patternCyrillic = Pattern.compile(cyrillicRegex);
        // ограничение введенных символов
        txtName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtName.getText().length() > 15) {
                txtName.setText(txtName.getText().substring(0, 15));
            }
            // Проверка на соответствие регулярному выражению
            if (!patternCyrillic.matcher(txtName.getText()).matches()) {
                // Если введенный текст не соответствует кириллице, очищаем поле ввода
                txtName.clear();
            }
        });

        String numberRegex = "^\\d+$";
        Pattern patternNumber = Pattern.compile(numberRegex);

        txtAge.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (txtAge.getText().length() > 2) {
                    txtAge.setText(txtAge.getText().substring(0, 2));
                } else if (!patternNumber.matcher(txtAge.getText()).matches()) {
                    // Если введенный текст не является числом, очищаем поле ввода
                    txtAge.clear();
                }
            }
            catch (Exception e) {
                System.err.println("Ошибка записи возраста!");
            }
        });

        // список запрещенных пород
        List<String> blockedWords = Arrays.asList("кобра", "мамба", "тайпан", "гадюка", "варан", "листолаз",
                "квакша", "скорпион", "каракурт", "артакс", "черная вдова", "тарантул", "волк", "пума",
                "сервал", "гепард", "лис", "гиен", "гиббон", "гоминид", "павиан", "макак", "ревун",
                "дикобраз", "журавлин", "сокол", "филин");

        txtBreed.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtBreed.getText().length() > 25) {
                txtBreed.setText(txtBreed.getText().substring(0, 25));
            }

            // Проверка на наличие запрещенного слова
            boolean isBlocked = blockedWords.stream()
                    .anyMatch(blockedWord -> txtBreed.getText().toLowerCase().contains(blockedWord));

            // Блокировка кнопки добавления, если слово найдено
            if (isBlocked)
            {
                btnAdd.setDisable(isBlocked);
                lblError.setText("Вид запрещён!");
            }
            else
            {
                btnAdd.setDisable(isBlocked);
                lblError.setText("");
            }
        });

        txtColor.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtColor.getText().length() > 30) {
                txtColor.setText(txtColor.getText().substring(0, 30));
            }
        });

        txtDesc.textProperty().addListener((observable, oldValue, newValue) -> {
            if (txtDesc.getText().length() > 300) {
                txtDesc.setText(txtDesc.getText().substring(0, 300));
            }
        });

        getData();
        // установка данных для выбора семейства
        ObservableList<String> animFams = FXCollections.observableArrayList(animalKinds);
        cmbFamily.setItems(animFams);
        cmbFamily.setValue(cmbFamily.getItems().get(0));
    }

    public void clearSpaces() {
        txtName.setText("");
        txtAge.setText("");
        txtBreed.setText("");
        txtColor.setText("");
        txtDesc.setText("");
        cmbFamily.setValue(cmbFamily.getItems().get(0));
    }

    @FXML
    private void openGiveFIOForm() {
        String thisName = txtName.getText();
        String thisAge = txtAge.getText();
        int thisKind = cmbFamily.getSelectionModel().selectedIndexProperty().getValue() + 1;
        String thisBreed = txtBreed.getText();
        String thisColor = txtColor.getText();
        String thisDescription = txtDesc.getText();
        boolean thisIllness = chbIllness.isSelected();
        boolean thisVaccinated = chbVaccinated.isSelected();
        boolean thisGender = chbGender.isSelected();
        if (!thisName.isEmpty() && !thisAge.isEmpty() && !thisBreed.isEmpty() &&
                !thisColor.isEmpty() && !thisDescription.isEmpty() && isImageAttached) {
            try {
                int ageValue = Integer.parseInt(thisAge);
                if (ageValue > 0 && ageValue < 25) {
                    currentAnimal = new Animal(thisName, Integer.parseInt(thisAge), thisKind, thisBreed,
                            thisColor, thisIllness, thisDescription, thisVaccinated, namePicture, thisGender);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("formGiveAnimalFIO.fxml"));
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

                    // Передаем экземпляр главной формы в FormGiveAnimalController
                    FormGiveAnimalFIOController controller = (FormGiveAnimalFIOController) loader.getController();
                    controller.initData(currentAnimal);
                    controller.setObserver(this);
                }
            else {
                lblError.setText("Неверный ввод!");
            }
            } catch (IOException e) {
                lblError.setText("Невозможно открыть форму или данные введены неверно!");
                e.printStackTrace();
            }
        }
        else {
            lblError.setText("Неверный ввод!");
        }
    }

    public void getData() {
        try {
            animalKinds.clear();
            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // создаем выражение, которое отражает реальное представление данных в базе данных
            // позволяющее создавать новые записи

            // получение семейств животных
            String selectAnimal = "SELECT family " +
                    "FROM public.animal_kinds;";
            PreparedStatement st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            ResultSet rs = st.executeQuery();
            int countRes = 0;
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

    @Override
    public void update() {
        System.out.println("Произошло обновление!");
        btnCancel.fire();
    }

    public FormGiveAnimalController() {
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