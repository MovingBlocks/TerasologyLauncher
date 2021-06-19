// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.ui;

public enum Warning {
    LOW_ON_SPACE("message_warning_lowOnSpace");

    private final String messageKey;

    Warning(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
