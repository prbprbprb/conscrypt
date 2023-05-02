package org.conscrypt

class VersionCode {
    public int major
    public int minor
    public int patch
    public String suffix

    VersionCode(String s) {
        String[] part = s.split("-")
        assert part.length > 0 && part.length < 3 : "Invalid version code string (1)"
        suffix = (part.length == 2) ? part[1] : ""
        String[] versions = part[0].split("\\.")
        assert versions.length == 3 : "Invalid version code string (2)"
        major = intval(versions[0])
        minor = intval(versions[1])
        patch = intval(versions[2])
    }

    private static int intval(String s) {
        assert s.isInteger()
        int value = s as int
        assert value >= 0
        return value
    }

    boolean isSnapShot() {
        return suffix == "SNAPSHOT"
    }
}