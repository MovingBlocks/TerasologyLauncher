// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import java.util.List;

public class JenkinsPayload {
    private JenkinsPayload() {

    }

    static String validPayload() {
        return "{\n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"artifacts\": [\n" +
                "        {\n" +
                "          \"fileName\": \"TerasologyOmega.zip\",\n" +
                "          \"relativePath\": \"distros/omega/build/distributions/TerasologyOmega.zip\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"fileName\": \"versionInfo.properties\",\n" +
                "          \"relativePath\": \"distros/omega/versionInfo.properties\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"number\": 1,\n" +
                "      \"result\": \"SUCCESS\",\n" +
                "      \"timestamp\": 1604285977306,\n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static String nullArtifactsPayload() {
        return "{ \n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"number\": 1, \"result\": \"SUCCESS\", \"timestamp\": 1604285977306, \n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static String emptyArtifactsPayload() {
        return "{\n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"artifacts\": [],\n" +
                "      \"number\": 1,\n" +
                "      \"result\": \"SUCCESS\",\n" +
                "      \"timestamp\": 1604285977306,\n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static String incompleteArtifactsPayload() {
        return "{\n" +
                "  \"builds\": [\n" +
                "    {\n" +
                "      \"artifacts\": [\n" +
                "        {\n" +
                "          \"fileName\": \"versionInfo.properties\",\n" +
                "          \"relativePath\": \"distros/omega/versionInfo.properties\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"number\": 1,\n" +
                "      \"result\": \"SUCCESS\",\n" +
                "      \"timestamp\": 1604285977306,\n" +
                "      \"url\": \"http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    static List<String> incompletePayloads() {
        return List.of(
                "{}",
                "{ \"builds\": [] }",
                nullArtifactsPayload(),
                emptyArtifactsPayload(),
                incompleteArtifactsPayload()
        );
    }
}
