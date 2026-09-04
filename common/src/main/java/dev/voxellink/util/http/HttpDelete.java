package dev.voxellink.util.http;

public class HttpDelete extends HttpClient {
    protected HttpDelete(String url) {
        super(url);
        this.method = HttpRequestMethod.DELETE;
    }
}
