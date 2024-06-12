package com.example.shelter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.lang.Thread;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

public class FormSummaryController implements Initializable {

    @FXML
    private Pane paneTop;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnWord;

    @FXML
    private Button btnExcel;

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

    public class ThreadWord extends Thread {
        @Override
        public void run() {
            try {
                lblError.setText("");
                String projectPath = System.getProperty("user.dir");
                String wordDir = projectPath + File.separator + "src" + File.separator +
                        "main" + File.separator + "resources" + File.separator +
                        "templates" + File.separator;

                XWPFDocument doc = new XWPFDocument(OPCPackage.open(wordDir + "template.docx"));

                for (XWPFParagraph p : doc.getParagraphs()) {
                    List<XWPFRun> runs = p.getRuns();
                    if (runs != null) {
                        for (XWPFRun r : runs) {
                            String text = r.getText(0);
                            if (text != null && text.contains("countAll")) {
                                text = text.replace("countAll", animalsCount + "");
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countNotIll")) {
                                text = text.replace("countNotIll", animalsHealthy + "");
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countVaccinated")) {
                                text = text.replace("countVaccinated", animalsVaccinated + "");
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countKind")) {
                                text = text.replace("countKind", animalKinds.get(animalsLargestKind));
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countPrice")) {
                                text = text.replace("countPrice", animalsExpenses + "");
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countMedicine")) {
                                text = text.replace("countMedicine", animalsExpensesMedicine + "");
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countGiven")) {
                                text = text.replace("countGiven", animalsGiven.toString());
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countHome")) {
                                text = text.replace("countHome", animalsHome.toString());
                                r.setText(text, 0);
                            }
                            if (text != null && text.contains("countCharity")) {
                                text = text.replace("countCharity", animalsCharity + "");
                                r.setText(text, 0);
                            }
                        }
                    }
                }
                FileOutputStream fos = new FileOutputStream(wordDir + "output.docx");
                doc.write(fos);
                fos.close();
            }
            catch (Exception e) {
                lblError.setText("Ошибка в файле шаблона Word!");
                e.printStackTrace();
            }
        }
    }

    public class ThreadExcel extends Thread {
        @Override
        public void run() {
            try {
                lblError.setText("");
                String projectPath = System.getProperty("user.dir");
                String excelDir = projectPath + File.separator + "src" + File.separator +
                        "main" + File.separator + "resources" + File.separator +
                        "templates" + File.separator;

                // запись в файл
                // без записи в этих ячейках невозможно занести необходимые данные
                XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(excelDir + "template.xlsx"));
                XSSFSheet sheet = wb.getSheetAt(0);
//                for (int r = 0; r < 4; r++) {
//                    XSSFRow row = sheet.createRow(r);
//                    for (int c = 0; c < 2; c++) {
//                        row.createCell(c).setCellValue(String.valueOf(2 * (r + c)));
//                    }
//                }
                FileOutputStream fos = new FileOutputStream(excelDir + "output.xlsx");
                wb.write(fos);
                fos.flush();
                fos.close();

                // модификация файла
                // изменяем первоначальные данные на необходимые
                sheet.getRow(15).getCell(6).setCellValue(animalsCount);
                sheet.getRow(16).getCell(6).setCellValue(animalsHealthy);
                sheet.getRow(17).getCell(6).setCellValue(animalsVaccinated);
                sheet.getRow(18).getCell(6).setCellValue(animalKinds.get(animalsLargestKind));

                sheet.getRow(22).getCell(6).setCellValue(animalsExpenses);
                sheet.getRow(23).getCell(6).setCellValue(animalsExpensesMedicine);

                sheet.getRow(28).getCell(2).setCellValue(animalsGiven.toString());
                sheet.getRow(30).getCell(2).setCellValue(animalsHome.toString());
                sheet.getRow(31).getCell(6).setCellValue(animalsCharity);

                try (FileOutputStream fOut = new FileOutputStream(excelDir + "output.xlsx")) {
                    wb.write(fOut);
                    fos.flush();
                    fos.close();
                }
            }
            catch (Exception e) {
                lblError.setText("Ошибка в файле шаблона Excel!");
                e.printStackTrace();
            }
        }
    }

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
        btnWord.setOnAction(event -> {
            new ThreadWord().start();
        } );
        btnExcel.setOnAction(event -> {
            new ThreadExcel().start();
        } );
        btnCancel.setOnAction(event -> {Stage stage = (Stage) paneTop.getScene().getWindow(); stage.close();});

        lblError.setText("");
        getData();
        if (animalsCount == 0) {
            lblError.setText("Животных не найдено!");
            btnWord.setDisable(true);
            btnExcel.setDisable(true);
        }
        else {
            getInfo();
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
}