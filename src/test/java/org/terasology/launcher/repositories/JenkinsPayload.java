// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.repositories;

import java.util.List;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class JenkinsPayload {

    /**
     * Example payloads from the "old" Jenkins at http://jenkins.terasology.org
     */
    static class V1 {
        static String minimalValidBuildPayload() {
            return """
                    {
                      "artifacts": [
                        {
                          "fileName": "TerasologyOmega.zip",
                          "relativePath": "distros/omega/build/distributions/TerasologyOmega.zip"
                        }
                      ],
                      "number": 1123,
                      "result": "SUCCESS",
                      "timestamp": 1609713454443,
                      "url": "http://jenkins.terasology.org/job/DistroOmega/1123/"
                    }
                    """;
        }

        static String validPayload() {
            return """
                    {
                      "builds": [
                        {
                          "actions": [
                            {
                              "causes": [
                                {
                                  "upstreamBuild": 2325
                                }
                              ]
                            }
                          ],
                          "artifacts": [
                            {
                              "fileName": "TerasologyOmega.zip",
                              "relativePath": "distros/omega/build/distributions/TerasologyOmega.zip"
                            }
                          ],
                          "number": 1123,
                          "result": "SUCCESS",
                          "timestamp": 1609713454443,
                          "url": "http://jenkins.terasology.org/job/DistroOmega/1123/",
                          "changeSet": {
                            "items": []
                          }
                        }
                      ],
                      "upstreamProjects": [
                        { "_class": "hudson.model.FreeStyleProject", "name": "Terasology" }
                      ]
                    }
                    """;
        }

        /**
         * Missing build.actions, build.changeSet, and upstreamProjects
         */
        static String minimalValidPayload() {
            return """
                    {
                      "builds": [
                        {
                          "artifacts": [
                            {
                              "fileName": "TerasologyOmega.zip",
                              "relativePath": "distros/omega/build/distributions/TerasologyOmega.zip"
                            }
                          ],
                          "number": 1123,
                          "result": "SUCCESS",
                          "timestamp": 1609713454443,
                          "url": "http://jenkins.terasology.org/job/DistroOmega/1123/"
                        }
                      ]
                    }""";
        }

        static String nullArtifactsPayload() {
            return """
                    {
                      "builds": [
                        {
                          "number": 1123,
                          "result": "SUCCESS",
                          "timestamp": 1609713454443,
                          "url": "http://jenkins.terasology.org/job/DistroOmega/1123/"
                        }
                      ]
                    }""";
        }

        static String emptyArtifactsPayload() {
            return """
                    {
                      "builds": [
                        {
                          "artifacts": [],
                          "number": 1123,
                          "result": "SUCCESS",
                          "timestamp": 1609713454443,
                          "url": "http://jenkins.terasology.org/job/DistroOmega/1123/"
                        }
                      ]
                    }""";
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
            return """
                    {
                      "builds": [
                        {
                          "artifacts": [
                            {
                              "fileName": "TerasologyOmega.zip",
                              "relativePath": "distros/omega/build/distributions/TerasologyOmega.zip"
                            },
                            {
                              "fileName": "versionInfo.properties",
                              "relativePath": "distros/omega/versionInfo.properties"
                            }
                          ],
                          "number": 1,
                          "result": "SUCCESS",
                          "timestamp": 1604285977306,
                          "url": "http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/"
                        }
                      ]
                    }""";
        }

        static String nullArtifactsPayload() {
            return """
                    { \s
                      "builds": [
                        {
                          "number": 1, "result": "SUCCESS", "timestamp": 1604285977306, \s
                          "url": "http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/"
                        }
                      ]
                    }""";
        }

        static String emptyArtifactsPayload() {
            return """
                    {
                      "builds": [
                        {
                          "artifacts": [],
                          "number": 1,
                          "result": "SUCCESS",
                          "timestamp": 1604285977306,
                          "url": "http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/"
                        }
                      ]
                    }""";
        }

        /**
         * Both artifacts {@code versionInfo.properties} and {@code TerasologyOmega.zip} are required, this is missing one of them.
         */
        static String incompleteArtifactsPayload() {
            return """
                    {
                      "builds": [
                        {
                          "artifacts": [
                            {
                              "fileName": "versionInfo.properties",
                              "relativePath": "distros/omega/versionInfo.properties"
                            }
                          ],
                          "number": 1,
                          "result": "SUCCESS",
                          "timestamp": 1604285977306,
                          "url": "http://jenkins.terasology.io/teraorg/job/Nanoware/job/Omega/job/develop/1/"
                        }
                      ]
                    }""";
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
