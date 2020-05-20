module org.terasology.launcher {
    //Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires java.naming;

    // Automatic modules
    requires org.commonmark;
    requires com.google.common;
    requires gson;
    requires java.sql; // gson requires it :(
    requires github.api;
    requires com.fasterxml.jackson.databind; // github.api required it
    requires org.everit.json.schema;
    requires org.json;
    requires semver4j;

    // openJavaFX and AWT
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;
    requires java.desktop;

    exports org.terasology.launcher; // for launcher run
    exports org.terasology.launcher.log to ch.qos.logback.core; // for TempLogFilePropertyDefiner
    opens org.terasology.launcher.gui.javafx to javafx.fxml; // for fxml controller access
    opens org.terasology.launcher.packages to gson;
}
