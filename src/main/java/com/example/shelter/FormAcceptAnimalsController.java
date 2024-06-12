package com.example.shelter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

public class FormAcceptAnimalsController implements Initializable, UpdateObserver {

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
    private Button btnSave;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnDeny;

    @FXML
    private Button btnLeft;

    @FXML
    private Button btnRight;

    @FXML
    private Button btnPicture;

    @FXML
    private ImageView imgBoxClose;

    @FXML
    private ImageView imgBoxUnshow;

    @FXML
    private Label lblError;

    double mouseX = 0, mouseY = 0;

    String namePicture;
    boolean isImageAttached = false;

    ArrayList<Animal> allAnimals = new ArrayList<>();
    ArrayList<String> animalKinds = new ArrayList<>();
    int animalsCount = 0;
    ArrayList<Integer> animalBroughtBy = new ArrayList<>();

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

        // заготовка для вывода возможных ошибок
        lblError.setText("");

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
        btnSave.setOnAction(event -> {
            saveCard();
            myObserver.update();
            getData();
            if (animalsCount != 0) {
                getInfo(1);
                btnLeft.setDisable(true);
                if (animalsCount == 1)
                    btnRight.setDisable(true);
            }
            else
                setDisableControls();
        });

        btnDeny.setOnAction(event -> {
            denyCard();
            myObserver.update();
            getData();
            if (animalsCount != 0) {
                getInfo(1);
                btnLeft.setDisable(true);
                if (animalsCount == 1)
                    btnRight.setDisable(true);
            }
            else
                setDisableControls();
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
                btnSave.setDisable(isBlocked);
                btnDeny.setDisable(isBlocked);
                lblError.setText("Вид запрещён!");
            }
            else
            {
                btnSave.setDisable(isBlocked);
                btnDeny.setDisable(isBlocked);
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

        txtId.setEditable(false);

        getData();
        // установка данных для выбора семейства
        ObservableList<String> animFams = FXCollections.observableArrayList(animalKinds);
        cmbFamily.setItems(animFams);
        if (animalsCount != 0) {
            getInfo(1);
            btnLeft.setDisable(true);
            if (animalsCount == 1)
                btnRight.setDisable(true);
        }
        else
        {
            setDisableControls();
        }
    }

    public void saveCard() {
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
                !thisColor.isEmpty() && !thisDescription.isEmpty()) {
            try {
                int ageValue = Integer.parseInt(thisAge);
                if (ageValue > 0 && ageValue < 25) {

                    Class.forName("org.postgresql.Driver"); // используемый класс
                    String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

                    Properties authorization = new Properties(); // свойства подключения к бд
                    authorization.setProperty("user", "dbuser1"); // логин
                    authorization.setProperty("password", "dbuser1"); // пароль
                    Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
                    // делим выполнение кода на две части - где есть изображение и где нет
                    int result;
                    if (isImageAttached) {
                        allAnimals.get(Integer.parseInt(txtId.getText()) - 1).setImage(namePicture);
                        String insertAnimal = "INSERT INTO animals (name, age, kind, breed, " +
                                "color_fur, is_ill, description, is_vaccinated, img, gender, " +
                                "is_brought, brought_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                        PreparedStatement st = connection.prepareStatement(insertAnimal);
                        // заполняем необходимые значения в подготовленном запросе
                        st.setString(1, thisName);
                        st.setInt(2, Integer.parseInt(thisAge));
                        st.setInt(3, thisKind);
                        st.setString(4, thisBreed);
                        st.setString(5, thisColor);
                        st.setBoolean(6, thisIllness);
                        st.setString(7, thisDescription);
                        st.setBoolean(8, thisVaccinated);
                        st.setString(9, namePicture);
                        st.setBoolean(10, thisGender);
                        st.setBoolean(11, true);
                        st.setInt(12, animalBroughtBy.get(Integer.parseInt(txtId.getText()) - 1));
                        // выполняем подготовленный запрос
                        result = st.executeUpdate();
                        // получаем результат
                        System.out.println("Запись успешно добавлена. Изменено строк: " + result);
                        st.close(); // закрываем выражение
                        // очищаем поля
                        lblError.setText("");
                    } else {
                        String insertAnimal = "INSERT INTO animals (name, age, kind, breed, " +
                                "color_fur, is_ill, description, is_vaccinated, img, gender, " +
                                "is_brought, brought_by) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
                        PreparedStatement st = connection.prepareStatement(insertAnimal);
                        // заполняем необходимые значения в подготовленном запросе
                        st.setString(1, thisName);
                        st.setInt(2, Integer.parseInt(thisAge));
                        st.setInt(3, thisKind);
                        st.setString(4, thisBreed);
                        st.setString(5, thisColor);
                        st.setBoolean(6, thisIllness);
                        st.setString(7, thisDescription);
                        st.setBoolean(8, thisVaccinated);
                        st.setString(9, allAnimals.get(Integer.parseInt(txtId.getText()) - 1).getImage());
                        st.setBoolean(10, thisGender);
                        st.setBoolean(11, true);
                        st.setInt(12, animalBroughtBy.get(Integer.parseInt(txtId.getText()) - 1));
                        // выполняем подготовленный запрос
                        result = st.executeUpdate();
                        // получаем результат
                        System.out.println("Запись успешно добавлена. Изменено строк: " + result);
                        st.close(); // закрываем выражение
                        // очищаем поля
                        lblError.setText("");
                    }
                    String updateAnimal = "UPDATE expenses SET animal_count = animal_count + 1 " +
                            " WHERE id_kind = ?;";
                    PreparedStatement st = connection.prepareStatement(updateAnimal);
                    // заполняем необходимые значения в подготовленном запрос
                    st.setInt(1, thisKind);
                    result = st.executeUpdate();
                    System.out.println("Запись в expenses успешно добавлена. Изменено строк: " + result);

                    // создаем выражение, которое отражает реальное представление данных в базе данных
                    updateAnimal = "UPDATE expenses SET sum_kind = animal_count * eat_per_month * money_per_food " +
                            " WHERE id_kind = ?;";
                    st = connection.prepareStatement(updateAnimal);
                    // заполняем необходимые значения в подготовленном запрос
                    st.setInt(1, thisKind);
                    result = st.executeUpdate();
                    System.out.println("Сумма в еxpenses обновлена. Изменено строк: " + result);

                    String deleteAnimal = "DELETE FROM public.animals_suggested WHERE id = ?;";
                    st = connection.prepareStatement(deleteAnimal);
                    st.setInt(1, allAnimals.get(Integer.parseInt(txtId.getText()) - 1).getDbId());
                    // выполняем подготовленный запрос
                    result = st.executeUpdate();
                    // получаем результат
                    System.out.println("Записей удалено: " + result);
                    st.close(); // закрываем выражение

                    st.close(); // закрываем выражение
                    connection.close();
                    lblError.setText("");
                }
                else {
                    lblError.setText("Неверный возраст!");
                }
            }
            catch (Exception e) {
                lblError.setText("Ошибка доступа к БД!");
                System.err.println("Ошибка доступа к БД!");
                e.printStackTrace();
            }
        }
        else {
            lblError.setText("Неверный ввод!");
        }
    }

    public void denyCard() {
        try {
            Class.forName("org.postgresql.Driver"); // используемый класс
            String url = "jdbc:postgresql://127.0.0.1:5498/animalshelter"; // строка подключения

            Properties authorization = new Properties(); // свойства подключения к бд
            authorization.setProperty("user", "dbuser1"); // логин
            authorization.setProperty("password", "dbuser1"); // пароль
            Connection connection = DriverManager.getConnection(url, authorization); // поле подключения
            // делим выполнение кода на две части - где есть изображение и где нет

            String deleteAnimal = "DELETE FROM public.animals_suggested WHERE id = ?;";
            PreparedStatement st = connection.prepareStatement(deleteAnimal);
            st.setInt(1, allAnimals.get(Integer.parseInt(txtId.getText()) - 1).getDbId());
            // выполняем подготовленный запрос
            int result = st.executeUpdate();
            // получаем результат
            System.out.println("Записей удалено: " + result);
            st.close(); // закрываем выражение
        }
        catch (Exception e) {
            lblError.setText("Ошибка доступа к БД!");
            System.err.println("Ошибка доступа к БД!");
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
                    "description, is_vaccinated, img, id, gender, brought_by " +
                    "FROM public.animals_suggested;";
            PreparedStatement st = connection.prepareStatement(selectAnimal);
            // выполняем подготовленный запрос
            ResultSet rs = st.executeQuery();
            int countRes = 0;
            while(rs.next()) {
                Animal animal = new Animal(rs.getString(1), rs.getInt(2), rs.getInt(3),
                        rs.getString(4), rs.getString(5), rs.getBoolean(6),
                        rs.getString(7), rs.getBoolean(8),
                        rs.getString(9), rs.getInt(10), rs.getBoolean(11));
                animalBroughtBy.add(rs.getInt(12));
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
        if (animalsCount != 0) {
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
            namePicture = thisAnimal.getImage();
        }
        else {
            setDisableControls();
        }
    }

    public void setDisableControls() {
        txtId.setText("");
        txtName.setText("");
        txtAge.setText("");
        txtBreed.setText("");
        txtColor.setText("");
        txtDesc.setText("");
        cmbFamily.setValue(cmbFamily.getItems().get(0));
        chbGender.setDisable(true);
        chbIllness.setDisable(true);
        chbVaccinated.setDisable(true);

        btnDeny.setDisable(true);
        btnLeft.setDisable(true);
        btnRight.setDisable(true);
        btnSave.setDisable(true);
        lblError.setText("Животных не найдено!");
        lblError.setText("Животных не найдено!");
    }

    public void update() {
        // Уведомляем наблюдателя о том, что произошло обновление
        System.out.println("Информация успешно добавлена. Обновляю главную форму.");
        // Здесь код для обновления главной формы
    }
}