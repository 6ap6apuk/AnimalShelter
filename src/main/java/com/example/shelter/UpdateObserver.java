package com.example.shelter;

import javafx.beans.property.SimpleObjectProperty;

public interface UpdateObserver {
    SimpleObjectProperty<Boolean> updateRequired = new SimpleObjectProperty<>();

    void update();
}
