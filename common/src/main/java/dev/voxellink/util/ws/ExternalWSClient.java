package dev.voxellink.util.ws;

import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.IdentityHashMap;
import java.util.Map;

public class ExternalWSClient extends WebSocketClient {
    private final Map<String, Value> events = new IdentityHashMap<>();

    public ExternalWSClient(String url) throws URISyntaxException {
        super(new URI(url));
    }

    public ExternalWSClient(URI serverUri) {
        super(serverUri);
    }

    @HostAccess.Export
    public void addEvent(String eventName, Value method) {
        if (method.canExecute()) {
            events.put(eventName, method);
        }
    }

    @HostAccess.Export
    @Override
    public void connect() {
        super.connect();
    }

    @HostAccess.Export
    @Override
    public void send(String text) {
        super.send(text);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        for (Map.Entry<String, Value> entry : events.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("onOpen")) {
                entry.getValue().execute();
            }
        }
    }

    @Override
    public void onMessage(String s) {
        for (Map.Entry<String, Value> entry : events.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("onMessage")) {
                entry.getValue().execute(s);
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        for (Map.Entry<String, Value> entry : events.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("onClose")) {
                entry.getValue().execute();
            }
        }
    }

    @Override
    public void onError(Exception e) {
        for (Map.Entry<String, Value> entry : events.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("onError")) {
                entry.getValue().execute(e.toString());
            }
        }
    }
}
