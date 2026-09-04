package dev.voxellink.util.http;

import org.graalvm.polyglot.HostAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpClient {
    protected final String url;
    protected final Map<String, String> headers = new HashMap<>();
    protected HttpRequestMethod method;
    protected int connectTimeout = 5000;
    protected int readTimeout = 5000;

    protected HttpClient(String url) {
        this.url = url;
    }

    @HostAccess.Export
    public HttpClient header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    @HostAccess.Export
    public HttpClient connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @HostAccess.Export
    public HttpClient readTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @HostAccess.Export
    public HttpClient timeout(int timeout) {
        this.connectTimeout = timeout;
        this.readTimeout = timeout;
        return this;
    }

    @HostAccess.Export
    public HttpResult connect() throws IOException {
        URL url2 = new URL(url);
        HttpURLConnection connection =  (HttpURLConnection) url2.openConnection();
        connection.setRequestMethod(this.method.toString());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        int responseCode = connection.getResponseCode();
        try {
            StringBuilder response = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return new HttpResult(responseCode, response.toString());
        } catch (IOException e) {
            return new HttpResult(responseCode, null);
        }
    }
}
