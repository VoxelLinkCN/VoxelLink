package dev.voxellink.util.http;

import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpPost extends HttpClient {
    private String type;
    private String data;

    protected HttpPost(String url) {
        super(url);
        this.method = HttpRequestMethod.POST;
    }

    @HostAccess.Export
    public HttpPost body(@NotNull String type, @NotNull String data) {
        this.type = type;
        this.data = data;
        return this;
    }

    @HostAccess.Export
    @Override
    public HttpResult connect() throws IOException {
        URL url2 = new URL(url);
        HttpURLConnection connection =  (HttpURLConnection) url2.openConnection();
        connection.setRequestMethod(this.method.toString());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        if (type != null && data != null) {
            connection.setRequestProperty("Content-Type", type);
            connection.setDoOutput(true);
            connection.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
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
