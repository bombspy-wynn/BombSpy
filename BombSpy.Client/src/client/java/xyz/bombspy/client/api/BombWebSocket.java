package xyz.bombspy.client.api;

import com.google.gson.*;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import xyz.bombspy.client.models.BombDTO;

import java.net.URI;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

public class BombWebSocket extends WebSocketClient {
    private final Consumer<BombInfo> bombCallback;
    private final Consumer<Exception> errorCallback;
    private final Runnable remoteCloseCallback;

    public BombWebSocket(URI serverUri, Consumer<BombInfo> bombCallback, Consumer<Exception> errorCallback, Runnable remoteCloseCallback) {
        super(serverUri);
        this.bombCallback = bombCallback;
        this.errorCallback = errorCallback;
        this.remoteCloseCallback = remoteCloseCallback;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {}

    @Override
    public void onMessage(String message) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BombType.class, (JsonSerializer<BombType>) (src, type, context) ->
                        new JsonPrimitive(src.ordinal()))
                .registerTypeAdapter(BombType.class, (JsonDeserializer<BombType>) (json, type, context) ->
                        BombType.values()[json.getAsInt()])
                .registerTypeAdapter(Instant.class, (JsonSerializer<Instant>) (src, type, context) ->
                        new JsonPrimitive(src.toString()))
                .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, context) ->
                        Instant.parse(json.getAsString()))
                .create();
        BombDTO bombDTO = gson.fromJson(message, BombDTO.class);
        bombCallback.accept(bombDTO.toBombInfo());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (remote) remoteCloseCallback.run();
    }

    @Override
    public void onError(Exception e) {
        errorCallback.accept(e);
    }
}
