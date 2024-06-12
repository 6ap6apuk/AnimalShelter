package com.example.shelter;

public class Animal { // класс животного

    String name; // кличка
    boolean gender; // пол животного
    int age; // возраст
    int kind; // семейство
    String breed; // порода
    String color_fur; // окрас
    boolean isIll; // проверка на болезнь
    String description; // описание
    boolean isVaccinated; // проверка на прививку
    String img; // название изображения
    int dbId; // идентификатор в базе данных

    // -- конструкторы

    Animal(){
        name = "";
        age = 0;
    }

    Animal(String Animal_name, int Animal_age, int Animal_kind, String Animal_breed, String Animal_fur,
            boolean Animal_isIll, String Animal_description, boolean Animal_isVaccinated, String Animal_img,
            boolean Animal_gender){
        String lowerInputName = Animal_name.toLowerCase();
        if(lowerInputName.matches("[а-я]+") && Animal_age > 0 && Animal_age < 40
            && Animal_kind > 0 && Animal_kind < 10)
        {
            char firstChar = Character.toUpperCase(lowerInputName.charAt(0));
            lowerInputName = firstChar + lowerInputName.substring(1);
            name = lowerInputName;
            gender = Animal_gender;
            age = Animal_age;
            kind = Animal_kind;
            breed = Animal_breed;
            color_fur = Animal_fur;
            isIll = Animal_isIll;
            description = Animal_description;
            isVaccinated = Animal_isVaccinated;
            img = Animal_img;
        }
    }

    Animal(String Animal_name, int Animal_age, int Animal_kind, String Animal_breed, String Animal_fur,
           boolean Animal_isIll, String Animal_description, boolean Animal_isVaccinated, String Animal_img,
           int Animal_dbId, boolean Animal_gender){
        String lowerInputName = Animal_name.toLowerCase();
        if(lowerInputName.matches("[а-я]+") && Animal_age > 0 && Animal_age < 25
                && Animal_kind > 0 && Animal_kind < 10)
        {
            char firstChar = Character.toUpperCase(lowerInputName.charAt(0));
            lowerInputName = firstChar + lowerInputName.substring(1);
            name = lowerInputName;
            gender = Animal_gender;
            age = Animal_age;
            kind = Animal_kind;
            breed = Animal_breed;
            color_fur = Animal_fur;
            isIll = Animal_isIll;
            description = Animal_description;
            isVaccinated = Animal_isVaccinated;
            img = Animal_img;
            dbId = Animal_dbId;
        }
    }

    // -- методы получения данных

    int getAge() { return age; }
    int getKind() { return kind; }
    boolean getGender() { return gender; }
    boolean getIll() { return isIll; }
    boolean getVaccinated() { return isVaccinated; }
    String getName() { return name; }
    String getBreed() { return breed; }
    String getColor() { return color_fur; }
    String getDescription() { return description; }
    String getImage() { return img; }
    int getDbId() { return dbId; }

    // -- методы занесения данных

    void setAge(int Animal_age){
        if(Animal_age > 0 && Animal_age < 25)
            age = Animal_age;
    }

    void setKind(int Animal_kind){
        if(Animal_kind > 0 && Animal_kind < 10)
            kind = Animal_kind;
    }
    void setGender(boolean Animal_gender) { gender = Animal_gender; }
    void setIll(boolean Animal_ill) { isIll = Animal_ill; }
    void setVaccinated(boolean Animal_vaccinated) { isVaccinated = Animal_vaccinated; }
    void setName(String Animal_name) { name = checkString(Animal_name); }
    void setBreed(String Animal_breed) { breed = checkString(Animal_breed); }
    void setColor(String Animal_color) { color_fur = checkString(Animal_color); }
    void setDescription(String Animal_desc) { description = Animal_desc; }
    void setImage(String Animal_img) { img = Animal_img; }
    void setDbId(int Animal_dbId) { dbId = Animal_dbId; }

    String checkString(String string){ // проверка на наличие кириллицы и правильного написания
        String lowerInput = string.toLowerCase();
        if (lowerInput.matches("[а-я]+")) {
            lowerInput = lowerInput.substring(1, lowerInput.length());
            lowerInput += Integer.toString(lowerInput.charAt(0) - 32);
            return lowerInput;
        }
        else
            return "Ошибка";
    }
}