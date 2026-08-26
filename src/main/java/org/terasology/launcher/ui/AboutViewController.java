// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import com.google.common.io.Files;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import org.jspecify.annotations.Nullable;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.launcher.platform.Platform;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.UnicodeEmojiImages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Controller for the <b>About</b> section in the tab view.
 * <p>
 * Presents static content which is compiled from Markdown and HTML documents.
 * <p>
 * Rendered as HTML in a {@code WebView} where available; falls back to plain text (Jsoup-stripped
 * for HTML) on platforms without WebKit, notably Windows/aarch64 - see JDK-8314064.
 */
// aboutInfoAccordion is injected by FXMLLoader after construction, before initialize() runs.
@SuppressWarnings("NullAway.Init")
public class AboutViewController {

    private static final Logger logger = LoggerFactory.getLogger(AboutViewController.class);

    /**
     * Bundle key for the resources related to this view.
     */
    private static final String ABOUT = "about";

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final boolean WEB_SUPPORTED = Platform.currentSupportsWebView();

    @FXML
    private Accordion aboutInfoAccordion;

    private final Parser parser;
    private final HtmlRenderer renderer;

    public AboutViewController() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(EmojiExtension.create()));
        // Default is IMAGE_ONLY with USE_SHORTCUT_TYPE=EMOJI_CHEAT_SHEET, rendering a relative path
        // like "<img src="/img/rocket.png" ...>" with no image root configured, so it never resolves.
        // Rendering as a Unicode character instead avoids that, but JavaFX's bundled WebKit can't
        // render supplementary-plane emoji (anything outside the BMP, e.g. rocket/toolbox) - they
        // show as tofu boxes regardless of the system font. Use GitHub's own emoji CDN images
        // instead: real PNGs, so WebView's font support is irrelevant.
        options.set(EmojiExtension.USE_SHORTCUT_TYPE, EmojiShortcutType.GITHUB);
        options.set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.IMAGE_ONLY);
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    @FXML
    public void initialize() {
        update();
    }

    /**
     * Update/reload the <b>About</b> view.
     * <p>
     * This will reload and parse the files to display again!
     */
    public void update() {
        aboutInfoAccordion.getPanes().clear();

        Stream.of("README.md", "CHANGELOG.md", "CONTRIBUTING.md", "LICENSE")
                .map(filename -> I18N.getFXMLUrl(ABOUT, filename))
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
                    final AnchorPane pane = new AnchorPane();
                    AnchorPane.setBottomAnchor(view, 0.0);
                    AnchorPane.setTopAnchor(view, 0.0);
                    AnchorPane.setLeftAnchor(view, 0.0);
                    AnchorPane.setRightAnchor(view, 0.0);
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

    private Optional<javafx.scene.Node> createViewFor(URL url) {
        String extension = Files.getFileExtension(url.getFile().toLowerCase(Locale.ROOT));
        if (WEB_SUPPORTED) {
            return switch (extension) {
                case "md", "markdown" -> renderMarkdown(url);
                case "htm", "html" -> renderHtml(url);
                default -> renderUnknown(url);
            };
        }
        return switch (extension) {
            case "htm", "html" -> renderHtmlAsText(url);
            default -> renderPlainText(url);
        };
    }

    // -- WebView-based rendering (default, where javafx.web is supported) --

    private Optional<javafx.scene.Node> renderMarkdown(URL url) {
        WebView view = null;
        try (InputStream input = url.openStream()) {
            view = new WebView();
            String markdown = new String(input.readAllBytes(), UTF_8);
            // GitHub release notes sometimes use literal emoji characters instead of :shortcode:
            // syntax - flexmark's emoji extension only handles the latter, so replace the former
            // with GitHub CDN images first (see UnicodeEmojiImages).
            Node document = parser.parse(UnicodeEmojiImages.replace(markdown));
            String content = "<body style='padding-left:24px;'>\n" + renderer.render(document) + "</body>";
            view.getEngine().loadContent(content, "text/html");
        } catch (IOException e) {
            logger.warn("Could not render markdown file: {}", url);
        }
        return finishWebView(view);
    }

    private Optional<javafx.scene.Node> renderHtml(URL url) {
        final WebView view = new WebView();
        view.getEngine().load(url.toExternalForm());
        return finishWebView(view);
    }

    private Optional<javafx.scene.Node> renderUnknown(URL url) {
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
        return finishWebView(view);
    }

    private Optional<javafx.scene.Node> finishWebView(@Nullable WebView view) {
        if (view == null) {
            return Optional.empty();
        }
        view.getStylesheets().add(I18N.getFXMLUrl("css_webview").toExternalForm());
        view.setContextMenuEnabled(false);
        return Optional.of(view);
    }

    // -- Plain-text fallback (platforms without javafx.web, e.g. Windows/aarch64) --

    private Optional<javafx.scene.Node> renderHtmlAsText(URL url) {
        try (InputStream input = url.openStream()) {
            String html = new String(input.readAllBytes(), UTF_8);
            return Optional.of(makeTextArea(Jsoup.parse(html).text()));
        } catch (IOException e) {
            logger.warn("Could not render file: {}", url);
            return Optional.empty();
        }
    }

    private Optional<javafx.scene.Node> renderPlainText(URL url) {
        try (InputStream input = url.openStream()) {
            return Optional.of(makeTextArea(new String(input.readAllBytes(), UTF_8)));
        } catch (IOException e) {
            logger.warn("Could not render file: {}", url);
            return Optional.empty();
        }
    }

    private TextArea makeTextArea(String content) {
        final TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        return textArea;
    }
}
