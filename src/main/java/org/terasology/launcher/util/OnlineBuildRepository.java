/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

public class OnlineBuildRepository implements IBuildRepository {

    private static final Logger logger = LoggerFactory.getLogger(OnlineBuildRepository.class);

    private static final String JENKINS_URL = "http://jenkins.terasology.org";
    private static final int JENKINS_TIMEOUT = 3000; // milliseconds


    public OnlineBuildRepository() {
    }

    @Override
    public boolean isJenkinsAvailable() {
        logger.trace("Checking Jenkins availability...");
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(JENKINS_URL).openConnection();
            try (AutoCloseable ac = conn::disconnect) {
                conn.setConnectTimeout(JENKINS_TIMEOUT);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    logger.trace("Jenkins is available at {}", JENKINS_URL);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Could not connect to Jenkins at {} - {}", JENKINS_URL, e.getMessage());
        }
        return false;
    }
}
