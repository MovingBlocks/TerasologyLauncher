// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import java.util.List;

public class JenkinsPayload {
    private JenkinsPayload() {

    }

    /**
     * Example payloads from the "old" Jenkins at http://jenkins.terasology.org
     */
    static class V1 {
        static String validPayload() {
            return "{\n" +
                    "  \"_class\": \"hudson.model.FreeStyleProject\",\n" +
                    "  \"builds\": [\n" +
                    "    {\n" +
                    "      \"_class\": \"hudson.model.FreeStyleBuild\",\n" +
                    "      \"actions\": [\n" +
                    "        {\n" +
                    "          \"_class\": \"hudson.model.CauseAction\",\n" +
                    "          \"causes\": [\n" +
                    "            {\n" +
                    "              \"_class\": \"hudson.model.Cause$UpstreamCause\",\n" +
                    "              \"upstreamBuild\": 2325\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"artifacts\": [\n" +
                    "        {\n" +
                    "          \"fileName\": \"md5sums.txt\",\n" +
                    "          \"relativePath\": \"distros/omega/build/distributions/md5sums.txt\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"fileName\": \"sha256sums.txt\",\n" +
                    "          \"relativePath\": \"distros/omega/build/distributions/sha256sums.txt\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"fileName\": \"TerasologyOmega.zip\",\n" +
                    "          \"relativePath\": \"distros/omega/build/distributions/TerasologyOmega.zip\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"number\": 1123,\n" +
                    "      \"result\": \"SUCCESS\",\n" +
                    "      \"timestamp\": 1609713454443,\n" +
                    "      \"url\": \"http://jenkins.terasology.org/job/DistroOmega/1123/\",\n" +
                    "      \"changeSet\": {\n" +
                    "        \"_class\": \"hudson.plugins.git.GitChangeSetList\",\n" +
                    "        \"items\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"upstreamProjects\": [\n" +
                    "    { \"_class\": \"hudson.model.FreeStyleProject\", \"name\": \"Terasology\" }\n" +
                    "  ]\n" +
                    "}\n";
        }
    }

    /**
     * Example payloads from the "new" Jenkins at http://jenkins.terasology.io
     */
    static class V2 {

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
