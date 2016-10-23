/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.launcher.util;

public enum LogLevel {

    DEFAULT("DEFAULT", "logLevel_default"),
    ALL("ALL", "logLevel_all"),
    //TRACE("TRACE", logLevel_trace"), unused - effectively identical to ALL
    DEBUG("DEBUG", "logLevel_debug"),
    INFO("INFO", "logLevel_info"),
    WARN("WARN", "logLevel_warn"),
    ERROR("ERROR", "logLevel_error"),
    OFF("OFF", "logLevel_off");

    private final String levelParameter;
    private final String labelKey;

    LogLevel(String sizeParameter, String labelKey) {
        this.levelParameter = sizeParameter;
        this.labelKey = labelKey;
    }

    public final boolean isDefault() {
        return this == DEFAULT;
    }

    public final String getLevelParameter() {
        return levelParameter;
    }

    @Override
    public final String toString() {
        return BundleUtils.getLabel(labelKey);
    }
}
