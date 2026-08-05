// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.fxml.FXML;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebView;
import org.terasology.launcher.util.I18N;

import java.util.Arrays;

public class ChangelogViewController {

    @FXML
    private WebView changelogView;

    private HtmlRenderer renderer;
    private Parser parser;

    public ChangelogViewController() {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(EmojiExtension.create()));
        // Default is IMAGE_ONLY, rendering "<img src="/img/rocket.png" ...>" - a relative path with
        // no configured image root, so it never resolves to anything real and shows as a broken-image
        // glyph in the WebView. Render as an actual Unicode character instead - no external resource
        // needed at all.
        options.set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.UNICODE_ONLY);
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
        Node document = parser.parse(changes);
        return renderer.render(document);
    }
}
