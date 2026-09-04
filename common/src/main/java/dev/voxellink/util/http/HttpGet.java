package dev.voxellink.util.http;

public class HttpGet extends HttpClient {
    protected HttpGet(String url) {
        super(url);
        this.method = HttpRequestMethod.GET;
    }
}
