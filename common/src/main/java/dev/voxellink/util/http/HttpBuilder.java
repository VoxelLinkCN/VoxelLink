package dev.voxellink.util.http;

import org.graalvm.polyglot.HostAccess;

public class HttpBuilder {
    protected final String url;

    protected HttpBuilder(String url) {
        this.url = url;
    }

    @HostAccess.Export
    public HttpGet get() {
        return new HttpGet(url);
    }

    @HostAccess.Export
    public HttpPost post() {
        return new HttpPost(url);
    }

    @HostAccess.Export
    public HttpPut put() {
        return new HttpPut(url);
    }

    @HostAccess.Export
    public HttpDelete delete() {
        return new HttpDelete(url);
    }

    @HostAccess.Export
    public static HttpBuilder builder(String url) {
        return new HttpBuilder(url);
    }

    public static class Factory {
        @HostAccess.Export
        public HttpBuilder builder(String url) {
            return HttpBuilder.builder(url);
        }
    }
}
