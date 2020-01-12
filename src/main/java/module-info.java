module terasology.launcher {
    //Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires java.naming;

    // Automatic modules
    requires CrashReporter;
    requires txtmark;
    requires com.google.common;
    requires gson;
    requires java.sql; // gson requires it :(
    requires jna;
    requires jna.platform;

    // openJavaFX and AWT
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;
    requires java.desktop;

    exports org.terasology.launcher; // for launcher run
    exports org.terasology.launcher.log to ch.qos.logback.core; // for TempLogFilePropertyDefiner
    opens org.terasology.launcher.gui.javafx to javafx.fxml; // for fxml controller access
}