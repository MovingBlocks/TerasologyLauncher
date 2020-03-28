package org.terasology.launcher.packages.db;

import java.io.Serializable;

public class DatabaseRepository implements Serializable {
    private String url;
    private String type;
    private PackageMetadata[] trackedPackages;

    public String getUrl() {
        return url;
    }

    public String getType() {
        return type;
    }

    public PackageMetadata[] getTrackedPackages() {
        return trackedPackages;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTrackedPackages(PackageMetadata[] trackedPackages) {
        this.trackedPackages = trackedPackages;
    }
}
