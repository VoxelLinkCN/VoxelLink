package dev.voxellink.util.http;

import org.graalvm.polyglot.HostAccess;

public enum HttpRequestMethod {
    GET,
    POST,
    PUT,
    DELETE;

    @HostAccess.Export
    @Override
    public String toString() {
        return super.toString();
    }
}
