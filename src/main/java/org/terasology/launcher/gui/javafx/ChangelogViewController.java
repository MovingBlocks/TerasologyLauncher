package org.terasology.launcher.gui.javafx;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebView;
import org.terasology.launcher.game.TerasologyGameVersion;
import org.terasology.launcher.util.BundleUtils;

public class ChangelogViewController {

    @FXML
    private WebView changelogView;

    //TODO: change this to a data type the package manager can handle
    final private Property<TerasologyGameVersion> gameVersion;

    public ChangelogViewController() {
        this.gameVersion = new SimpleObjectProperty<>();
        gameVersion.addListener((observableValue, oldVersion, newVersion) -> update(newVersion));
    }

    /**
     * Bin this changelog view to the game version described by the property.
     * <p>
     * The <b>ChangelogView</b> will update every time the property changes.
     *
     * @param version property denoting the game version to display the changelog for
     */
    public void bind(ReadOnlyProperty<TerasologyGameVersion> version) {
        gameVersion.bind(version);
    }

    /**
     * Update the displayed changelog based on the selected `gameVersion`.
     *
     * @param gameVersion
     */
    private void update(TerasologyGameVersion gameVersion) {
        final String gameInfoTextHTML;
        if ((gameVersion == null) || (gameVersion.getJob() == null) || (gameVersion.getBuildNumber() == null)) {
            gameInfoTextHTML = "";
        } else {
            gameInfoTextHTML = getGameInfoText(gameVersion);
        }

        changelogView.getEngine().loadContent(gameInfoTextHTML);
        changelogView.setBlendMode(BlendMode.LIGHTEN);
        changelogView.getEngine().setUserStyleSheetLocation(BundleUtils.getFXMLUrl("css_webview").toExternalForm());
    }

    private String getGameInfoText(TerasologyGameVersion gameVersion) {
//        logger.debug("Display game version: {} {}", gameVersion, gameVersion.getGameVersionInfo());
//
//        final Object[] arguments = new Object[9];
//        arguments[0] = gameVersion.getJob().name();
//        if (gameVersion.getJob().isStable()) {
//            arguments[1] = 1;
//        } else {
//            arguments[1] = 0;
//        }
//        arguments[2] = gameVersion.getJob().getGitBranch();
//        arguments[3] = gameVersion.getBuildNumber();
//        if (gameVersion.isLatest()) {
//            arguments[4] = 1;
//        } else {
//            arguments[4] = 0;
//        }
//        if (gameVersion.isInstalled()) {
//            arguments[5] = 1;
//        } else {
//            arguments[5] = 0;
//        }
//        if (gameVersion.getSuccessful() != null) {
//            if (!gameVersion.getSuccessful()) {
//                // faulty
//                arguments[6] = 0;
//            } else {
//                arguments[6] = 1;
//            }
//        } else {
//            // unknown
//            arguments[6] = 2;
//        }
//        if ((gameVersion.getGameVersionInfo() != null)
//                && (gameVersion.getGameVersionInfo().getDisplayVersion() != null)) {
//            arguments[7] = gameVersion.getGameVersionInfo().getDisplayVersion();
//        } else {
//            arguments[7] = "";
//        }
//        if ((gameVersion.getGameVersionInfo() != null)
//                && (gameVersion.getGameVersionInfo().getDateTime() != null)) {
//            arguments[8] = gameVersion.getGameVersionInfo().getDateTime();
//        } else {
//            arguments[8] = "";
//        }
//
//        final String infoHeader1 = BundleUtils.getMessage(gameVersion.getJob().getInfoMessageKey(), arguments);
//        final String infoHeader2 = BundleUtils.getMessage("infoHeader2", arguments);
//
//        final StringBuilder b = new StringBuilder();
//        if ((infoHeader1 != null) && (infoHeader1.trim().length() > 0)) {
//            b.append("<h1>")
//                    .append(escapeHtml(infoHeader1))
//                    .append("</h1>\n");
//        }
//        if ((infoHeader2 != null) && (infoHeader2.trim().length() > 0)) {
//            b.append("<h2>")
//                    .append(escapeHtml(infoHeader2))
//                    .append("</h2>\n");
//        }
//        b.append("<strong>\n")
//                .append(BundleUtils.getLabel("infoHeader3"))
//                .append("</strong>\n");
//
//        if ((gameVersion.getChangeLog() != null) && !gameVersion.getChangeLog().isEmpty()) {
//            b.append("<p>\n")
//                    .append(BundleUtils.getLabel("infoHeader4"))
//                    .append("<ul>\n");
//            for (String msg : gameVersion.getChangeLog()) {
//                b.append("<li>")
//                        .append(escapeHtml(msg))
//                        .append("</li>\n");
//            }
//            b.append("</ul>\n")
//                    .append("</p>\n");
//        }
//
//        /* Append changelogs of previous builds. */
//        int previousLogs = gameVersion.getJob().isStable() ? 1 : 10;
//        b.append("<hr/>");
//        for (String msg : packageManager.getAggregatedChangeLog(gameVersion, previousLogs)) {
//            b.append("<li>")
//                    .append(escapeHtml(msg))
//                    .append("</li>\n");
//        }
//
//        return b.toString();
        return "WIP";
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
    }
}
