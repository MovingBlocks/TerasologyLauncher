module terasology.launcher {
    //Logging
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires java.naming;

    requires txtmark;
    requires com.google.common;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.web;
    requires CrashReporter;
    requires gson;
    requires ch.qos.logback.core;
    requires java.desktop;
    requires jna;
    requires jna.platform;

    exports org.terasology.launcher;
}