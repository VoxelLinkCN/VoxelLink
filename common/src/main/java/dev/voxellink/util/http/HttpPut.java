package dev.voxellink.util.http;

public class HttpPut extends HttpClient {
    protected HttpPut(String url) {
        super(url);
        this.method = HttpRequestMethod.PUT;
    }
}
