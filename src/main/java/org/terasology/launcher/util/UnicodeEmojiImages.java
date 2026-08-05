// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

/**
 * GitHub release notes sometimes contain literal Unicode emoji characters (e.g. copy-pasted rocket/toolbox
 * symbols in section headers) rather than {@code :shortcode:} markdown syntax. flexmark's emoji extension
 * only recognizes shortcodes, so literal emoji pass through untouched - and JavaFX's bundled WebKit can't
 * render supplementary-plane emoji (anything outside the Basic Multilingual Plane) regardless of the system
 * font, showing replacement-character boxes instead.
 * <p>
 * This replaces such characters with an {@code <img>} tag pointing at GitHub's own emoji CDN before the
 * markdown is parsed, so they render as real images instead - the same fix already applied to flexmark's
 * {@code :shortcode:} handling via {@code EmojiShortcutType.GITHUB}.
 */
public final class UnicodeEmojiImages {

    private static final String GITHUB_EMOJI_CDN = "https://github.githubassets.com/images/icons/emoji/unicode/";

    private UnicodeEmojiImages() {
    }

    public static String replace(String markdown) {
        StringBuilder result = new StringBuilder(markdown.length());
        int i = 0;
        while (i < markdown.length()) {
            int codePoint = markdown.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            if (isSupplementaryPlaneEmoji(codePoint)) {
                result.append("<img src=\"")
                        .append(GITHUB_EMOJI_CDN)
                        .append(Integer.toHexString(codePoint))
                        .append(".png?v8\" alt=\"\" height=\"20\" width=\"20\" align=\"absmiddle\" />");
            } else {
                result.appendCodePoint(codePoint);
            }
            i += charCount;
        }
        return result.toString();
    }

    // Covers the emoji-carrying supplementary-plane blocks (Mahjong/Domino/Playing Cards, Enclosed
    // Alphanumeric Supplement, Enclosed Ideographic Supplement, Misc Symbols & Pictographs, Emoticons,
    // Transport & Map Symbols, regional-indicator flag letters, Supplemental Symbols & Pictographs,
    // Symbols & Pictographs Extended-A) without touching unrelated supplementary-plane text such as
    // rare CJK ideographs.
    private static boolean isSupplementaryPlaneEmoji(int codePoint) {
        return codePoint >= 0x1F000 && codePoint <= 0x1FFFF;
    }
}
