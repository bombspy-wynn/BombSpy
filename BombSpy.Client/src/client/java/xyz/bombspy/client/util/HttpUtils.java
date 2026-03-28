package xyz.bombspy.client.util;

import com.google.gson.*;
import com.wynntils.models.worlds.type.BombType;
import xyz.bombspy.client.core.BombSpyClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpUtils {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public static CompletableFuture<HttpResponse<String>> put(URI uri, Object payload) {
        return put(uri, new HashMap<>(), payload);
    }

    public static CompletableFuture<HttpResponse<String>> put(URI uri, Map<String, String> headers, Object payload) {
        BombSpyClient.LOGGER.debug("Sending data to endpoint: {}", uri);

        String payloadString;
        try {
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
            payloadString = gson.toJson(payload);
        } catch (Exception e) {
            BombSpyClient.LOGGER.error("Failed to serialize object for POST request for endpoint '{}': {}", uri, e.getMessage());
            return CompletableFuture.completedFuture(null);
        }

        HttpRequest request;
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(payloadString));

            for (Map.Entry<String, String> header : headers.entrySet())
                requestBuilder = requestBuilder.header(header.getKey(), header.getValue());

            request = requestBuilder.build();
        } catch (Exception e) {
            BombSpyClient.LOGGER.error("Failed to create POST request for endpoint '{}': {}", uri, e.getMessage());
            return CompletableFuture.completedFuture(null);
        }

        BombSpyClient.LOGGER.debug("Sending payload: {}", payloadString);
        return send(request);
    }

    public static CompletableFuture<HttpResponse<String>> get(URI uri) {
        return get(uri, new HashMap<>());
    }

    public static CompletableFuture<HttpResponse<String>> get(URI uri, Map<String, String> headers) {
        BombSpyClient.LOGGER.debug("Fetching data from endpoint: {}", uri);
        HttpRequest request;

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(TIMEOUT)
                    .GET();

            for (Map.Entry<String, String> header : headers.entrySet())
                requestBuilder = requestBuilder.header(header.getKey(), header.getValue());

            request = requestBuilder.build();
        } catch (Exception e) {
            BombSpyClient.LOGGER.error("Failed to create GET request for endpoint '{}': {}", uri, e.getMessage());
            return CompletableFuture.completedFuture(null);
        }

        return send(request).whenComplete((resp, ex) -> {
            int code = resp.statusCode();
            if (code < 200 || code >= 300) {
                BombSpyClient.LOGGER.error("Failed to GET from endpoint '{}'. Code '{}', Reason '{}'", uri, code, resp.body());
            }
        });
    }

    private static CompletableFuture<HttpResponse<String>> send(HttpRequest request) {
        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
