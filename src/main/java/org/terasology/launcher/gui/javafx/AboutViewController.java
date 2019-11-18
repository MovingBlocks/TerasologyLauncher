package org.terasology.launcher.gui.javafx;

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.google.common.io.Files;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.util.BundleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Controller for the <b>About</b> section in the tab view.
 *
 * Presents static content which is compiled from Markdown and HTML documents.
 */
public class AboutViewController {

    private static final Logger logger = LoggerFactory.getLogger(AboutViewController.class);

    /** Bundle key for the resources related to this view. */
    private static final String ABOUT = "about";

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @FXML
    private Accordion aboutInfoAccordion;

    @FXML
    public void initialize() {
        update();
    }

    /**
     * Update/reload the <b>About</b> view.
     *
     * This will reload and parse the files to display again!
     */
    public void update() {
        aboutInfoAccordion.getPanes().clear();

        Stream.of("README.md", "CHANGELOG.md", "CONTRIBUTING.md", "LICENSE")
                .map(filename -> BundleUtils.getFXMLUrl(ABOUT, filename))
                .filter(Objects::nonNull)
                .map(this::createPaneFor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(aboutInfoAccordion.getPanes()::add);

        if (!aboutInfoAccordion.getPanes().isEmpty()) {
            aboutInfoAccordion.setExpandedPane(aboutInfoAccordion.getPanes().get(0));
        }
    }

    private Optional<TitledPane> createPaneFor(URL url) {
        return createViewFor(url)
                .map(view -> {
                    view.getStylesheets().add(BundleUtils.getFXMLUrl("css_webview").toExternalForm());
                    view.setContextMenuEnabled(false);
                    return view;
                })
                .map(view -> {
                    final AnchorPane pane = new AnchorPane();
                    AnchorPane.setBottomAnchor(view, 0.0);
                    AnchorPane.setTopAnchor(view, 0.0);
                    pane.getChildren().add(view);
                    return pane;
                })
                .map(contentPane -> {
                    String fname = Files.getNameWithoutExtension(url.getFile());
                    final TitledPane titledPane = new TitledPane(fname, contentPane);
                    titledPane.setAnimated(false);
                    return titledPane;
                });
    }

    private Optional<WebView> createViewFor(URL url) {
        switch (Files.getFileExtension(url.getFile().toLowerCase())) {
            case "md":
            case "markdown":
                return renderMarkdown(url);
            case "htm":
            case "html":
                return renderHtml(url);
            default:
                return renderUnknown(url);
        }

    }

    private Optional<WebView> renderMarkdown(URL url) {
        WebView view = null;
        try (InputStream input = url.openStream()) {
            view = new WebView();
            String content = new StringBuilder()
                    .append("<body style='padding-left:24px;'>\n")
                    .append(Processor.process(input, Configuration.DEFAULT))
                    .append("</body>")
                    .toString();
            view.getEngine().loadContent(content, "text/html");
        } catch (IOException e) {
            logger.warn("Could not render markdown file: {}", url);
        }
        return Optional.ofNullable(view);
    }

    private Optional<WebView> renderHtml(URL url) {
        final WebView view = new WebView();
        view.getEngine().load(url.toExternalForm());
        return Optional.of(view);
    }

    private Optional<WebView> renderUnknown(URL url) {
        WebView view = null;
        try (Reader isr = new InputStreamReader(url.openStream(), UTF_8);
             BufferedReader br = new BufferedReader(isr)) {

            view = new WebView();
            StringBuilder content = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                content.append(line);
                content.append(System.lineSeparator());
                line = br.readLine();
            }
            view.getEngine().loadContent(content.toString(), "text/plain");
        } catch (IOException e) {
            logger.warn("Could not render file: {}", url);
        }
        return Optional.ofNullable(view);
    }
}
