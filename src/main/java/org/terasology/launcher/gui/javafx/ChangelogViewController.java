/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        changes.forEach(change -> builder.append("<li>").append(escapeHtml(change)).append("</li>"));
        builder.append("</ul>");

        return builder.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
    }
}
