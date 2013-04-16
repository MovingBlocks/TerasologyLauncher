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

package org.terasologylauncher.changelog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasologylauncher.BuildType;
import org.terasologylauncher.util.DownloadUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * @author MrBarsack
 */
public final class Changelog {

    private static final Logger logger = LoggerFactory.getLogger(Changelog.class);

    private static final String WRAPPER
        = "/api/xml?xpath=//changeSet/item/msg[1]|//changeSet/item/author[1]/fullName&wrapper=msgs";

    private Changelog() {
    }

    public static Document getChangelog(final BuildType buildType, final int version) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        // TODO Change version "-1" into "Latest..."

        logger.debug("Changelog: " + buildType + " " + version);

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace(); // TODO Logging
        }
        try {
            if (buildType == BuildType.NIGHTLY) {
                return builder.parse(DownloadUtils.getURL(DownloadUtils.TERASOLOGY_NIGHTLY_JOB_NAME, version,
                    WRAPPER).openStream());
            } else {
                return builder.parse(DownloadUtils.getURL(DownloadUtils.TERASOLOGY_STABLE_JOB_NAME, version,
                    WRAPPER).openStream());
            }
            // TODO close stream
        } catch (SAXException e) {
            e.printStackTrace(); // TODO Logging
        } catch (IOException e) {
            e.printStackTrace(); // TODO Logging
        }
        logger.error("Error while loading changelog.");
        return null;
    }
}
