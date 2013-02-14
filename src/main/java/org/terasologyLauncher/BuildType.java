package org.terasologyLauncher;

public enum BuildType {
    STABLE(0),
    NIGHTLY(1);

    private final int type;

    private BuildType(int type) {
        this.type = type;
    }

    public int type() {
        return type;
    }

    public static BuildType getType(int type) {
        for (BuildType t : values()){
            if (t.type == type){
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown build type: " + type);
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
