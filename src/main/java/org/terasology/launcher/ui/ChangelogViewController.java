// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.fxml.FXML;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebView;
import org.terasology.launcher.util.I18N;
import org.terasology.launcher.util.UnicodeEmojiImages;

import java.util.Arrays;

public class ChangelogViewController {

    @FXML
    private WebView changelogView;

    private HtmlRenderer renderer;
    private Parser parser;

    public ChangelogViewController() {
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

    /**
     * Update the displayed changelog based on the selected package.
     *
     * @param changes list of changes
     */
    void update(final String changes) {
        changelogView.getEngine().loadContent(makeHtml(changes));
        changelogView.setBlendMode(BlendMode.LIGHTEN);
        changelogView.getEngine().setUserStyleSheetLocation(I18N.getFXMLUrl("css_webview").toExternalForm());
    }

    private String makeHtml(final String changes) {
        // GitHub release notes sometimes use literal emoji characters in headers instead of
        // :shortcode: syntax - flexmark's emoji extension only handles the latter, so replace the
        // former with GitHub CDN images first (see UnicodeEmojiImages).
        Node document = parser.parse(UnicodeEmojiImages.replace(changes));
        return renderer.render(document);
    }
}
