module org.terasology.launcher {
    requires org.slf4j;
    requires gson;
    requires org.everit.json.schema;
    requires org.json;
    requires javafx.graphics;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires com.google.common;
    requires txtmark;

    exports org.terasology.launcher;
}