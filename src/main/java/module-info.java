module org.example.myTranslator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires org.json;
    requires java.desktop;

    opens org.example.myTranslator to javafx.fxml;
    exports org.example.myTranslator;
}