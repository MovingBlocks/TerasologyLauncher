package org.terasology.launcher.packages;

import com.google.common.collect.Lists;

import java.net.URI;
import java.util.List;

public class JenkinsReleaseHandler implements ReleaseHandler {

    private static final String TERASOLOGY_ZIP_PATTERN = "Terasology.*zip";
    private final URI url;

    private static final String API_FILTER = "?tree="
            + "builds["
            + "actions[causes[upstreamBuild]]{0},"
            + "number,"
            + "result,"
            + "artifacts[fileName,relativePath],"
            + "url,"
            + "changeSet[items[msg]]],"
            + "upstreamProjects[name]";

    public JenkinsReleaseHandler(URI baseUrl, String jobName) {
        url = baseUrl.resolve("job").resolve(jobName).resolve("api/json").resolve(API_FILTER);
    }

    @Override
    public List<Package> getPackages() {
        return Lists.newArrayList();
    }
}
