module doodlejump {
    requires javafx.controls;
    requires javafx.fxml;

    opens doodlejump to javafx.fxml;
    exports doodlejump;
}
