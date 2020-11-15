// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.gui.javafx;

import javafx.fxml.FXML;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebView;
import org.terasology.launcher.util.BundleUtils;

import java.util.List;

public class ChangelogViewController {

    @FXML
    private WebView changelogView;

    public ChangelogViewController() {
    }

    /**
     * Update the displayed changelog based on the selected package.
     *
     * @param changes list of changes
     */
    void update(final List<String> changes) {
        changelogView.getEngine().loadContent(makeHtml(changes));
        changelogView.setBlendMode(BlendMode.LIGHTEN);
        changelogView.getEngine().setUserStyleSheetLocation(BundleUtils.getFXMLUrl("css_webview").toExternalForm());
    }

    private String makeHtml(final List<String> changes) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<strong>")
                .append(BundleUtils.getLabel("infoHeader4"))
                .append("</strong>")
                .append("<ul>");
        if (changes != null) {
            changes.forEach(change -> builder.append("<li>").append(escapeHtml(change)).append("</li>"));
        }
        builder.append("</ul>");

        return builder.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
    }
}
