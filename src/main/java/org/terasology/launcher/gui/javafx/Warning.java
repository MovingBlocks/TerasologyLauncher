package org.terasology.launcher.gui.javafx;

public enum Warning {
    LOW_ON_SPACE("message_warning_lowOnSpace");

    public final String messageKey;

    Warning(String messageKey) {
        this.messageKey = messageKey;
    }
}
