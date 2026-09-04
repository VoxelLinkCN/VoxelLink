package dev.voxellink.util.ws;

import org.graalvm.polyglot.HostAccess;

import java.net.URISyntaxException;

public class ExternalWSUtil {
    @HostAccess.Export
    public ExternalWSClient client(String url) throws URISyntaxException {
        return new ExternalWSClient(url);
    }
}
