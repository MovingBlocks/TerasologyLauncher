// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import java.util.List;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class JenkinsPayload {
    private JenkinsPayload() {

    }

    /**
     * Example payloads from the "old" Jenkins at http://jenkins.terasology.org
     */
    static class V1 {
        static String minimalValidBuildPayload() {
            return "{\n" +
                    "  \"artifacts\": [\n" +
                    "    {\n" +
                    "      \"fileName\": \"TerasologyOmega.zip\",\n" +
                    "      \"relativePath\": \"distros/omega/build/distributions/TerasologyOmega.zip\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"number\": 1123,\n" +
                    "  \"result\": \"SUCCESS\",\n" +
                    "  \"timestamp\": 1609713454443,\n" +
                    "  \"url\": \"http://jenkins.terasology.org/job/DistroOmega/1123/\"\n" +
                    "}\n";
        }

        static String validPayload() {
            return "{\n" +
                    "  \"builds\": [\n" +
                    "    {\n" +
                    "      \"actions\": [\n" +
                    "        {\n" +
                    "          \"causes\": [\n" +
                    "            {\n" +
                    "              \"upstreamBuild\": 2325\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"artifacts\": [\n" +
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
                    "        \"items\": []\n" +
                    "      }\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"upstreamProjects\": [\n" +
                    "    { \"_class\": \"hudson.model.FreeStyleProject\", \"name\": \"Terasology\" }\n" +
                    "  ]\n" +
                    "}\n";
        }

        /**
         * Missing build.actions, build.changeSet, and upstreamProjects
         */
        static String minimalValidPayload() {
            return "{\n" +
                    "  \"builds\": [\n" +
                    "    {\n" +
                    "      \"artifacts\": [\n" +
                    "        {\n" +
                    "          \"fileName\": \"TerasologyOmega.zip\",\n" +
                    "          \"relativePath\": \"distros/omega/build/distributions/TerasologyOmega.zip\"\n" +
                    "        }\n" +
                    "      ],\n" +
                    "      \"number\": 1123,\n" +
                    "      \"result\": \"SUCCESS\",\n" +
                    "      \"timestamp\": 1609713454443,\n" +
                    "      \"url\": \"http://jenkins.terasology.org/job/DistroOmega/1123/\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }

        static String nullArtifactsPayload() {
            return "{\n" +
                    "  \"builds\": [\n" +
                    "    {\n" +
                    "      \"number\": 1123,\n" +
                    "      \"result\": \"SUCCESS\",\n" +
                    "      \"timestamp\": 1609713454443,\n" +
                    "      \"url\": \"http://jenkins.terasology.org/job/DistroOmega/1123/\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }

        static String emptyArtifactsPayload() {
            return "{\n" +
                    "  \"builds\": [\n" +
                    "    {\n" +
                    "      \"artifacts\": [],\n" +
                    "      \"number\": 1123,\n" +
                    "      \"result\": \"SUCCESS\",\n" +
                    "      \"timestamp\": 1609713454443,\n" +
                    "      \"url\": \"http://jenkins.terasology.org/job/DistroOmega/1123/\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
        }

        static List<String> incompatiblePayloads() {
            return List.of(
                    "{}",
                    "{ \"builds\": [] }",
                    nullArtifactsPayload(),
                    emptyArtifactsPayload()
            );
        }
    }

    /**
     * Example payloads from the "new" Jenkins at http://jenkins.terasology.io
     */
    static class V2 {

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

        /**
         * Both artifacts {@code versionInfo.properties} and {@code TerasologyOmega.zip} are required, this is missing one of them.
         */
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
}
