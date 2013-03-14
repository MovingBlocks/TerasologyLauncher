/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasologylauncher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.Languages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

public final class BundleUtil {

    private static final Logger logger = LoggerFactory.getLogger(BundleUtil.class);

    private static final String LABELS_BUNDLE = "org.terasologylauncher.bundle.LabelsBundle";
    private static final String URI_BUNDLE = "org.terasologylauncher.bundle.URIBundle";

    private BundleUtil() {
    }

    public static String getLabel(final String key) {
        return ResourceBundle.getBundle(LABELS_BUNDLE, Languages.getCurrentLocale()).getString(key);
    }

    public static URI getURI(final String key) {
        String uriStr = ResourceBundle.getBundle(URI_BUNDLE, Languages.getCurrentLocale()).getString(key);
        try {
            return new URI(uriStr);
        } catch (URISyntaxException e) {
            logger.error("Can not create the URI! " + uriStr, e);
        }
        return null;
    }
}
