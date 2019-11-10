package org.terasology.launcher.gui.javafx;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.text.DateFormat;
import java.util.Date;

public class LogView {

    private LogView() {}

    public static void updateLogView(TableView<ILoggingEvent> table, ObservableList<ILoggingEvent> log) {
        table.setItems(log);

        table.setEditable(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ILoggingEvent, Date> timestampColumn = new TableColumn<>("Timestamp");
        timestampColumn.setCellValueFactory(item -> new ReadOnlyObjectWrapper<>(new Date(item.getValue().getTimeStamp())));
        // Fix the width such that `CONSTRAINED_RESIZE_POLICY` only applies to the last column
        // TODO: setting fixed width is bad and might conflict with system settings such as font size
        setFixedWidth(timestampColumn, 120);
        timestampColumn.setCellFactory(column ->
                new TableCell<ILoggingEvent, Date>() {
                    private final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
                    @Override
                    protected void updateItem(Date item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty ? null : formatter.format(item));
                    }
                }
        );
        table.getColumns().add(timestampColumn);

        TableColumn<ILoggingEvent, Level> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(item -> new ReadOnlyObjectWrapper<>(item.getValue().getLevel()));
        // Fix the width such that `CONSTRAINED_RESIZE_POLICY` only applies to the last column
        // TODO: setting fixed width is bad and might conflict with system settings such as font size
        setFixedWidth(levelColumn, 50);
        table.getColumns().add(levelColumn);

        TableColumn<ILoggingEvent, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(item -> new ReadOnlyObjectWrapper<>(item.getValue().getFormattedMessage()));
        table.getColumns().add(messageColumn);
    }

    private static void setFixedWidth(TableColumn<?, ?> column, double width) {
        column.setMinWidth(width);
        column.setMaxWidth(width);
        column.setResizable(false);
    }
}
