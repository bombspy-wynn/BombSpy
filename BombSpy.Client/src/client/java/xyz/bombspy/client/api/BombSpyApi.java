package xyz.bombspy.client.api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.components.Managers;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import org.jspecify.annotations.Nullable;
import xyz.bombspy.client.core.BombSpyClient;
import xyz.bombspy.client.features.BombSpyFeature;
import xyz.bombspy.client.models.BombDTO;
import xyz.bombspy.client.util.HttpUtils;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class BombSpyApi {
    public static CompletableFuture<List<BombInfo>> getActiveBombs() {
        BombSpyFeature bombSpyFeature = Managers.Feature.getFeatureInstance(BombSpyFeature.class);

        URI uri = URI.create("http://" + bombSpyFeature.apiAddress.get());

        return HttpUtils.get(uri)
                .thenApply(response -> {
                    List<BombDTO> bombDTOList = handleResponse(response, responseBody -> parseResponseBody(responseBody, new TypeToken<List<BombDTO>>() {}.getType()));
                    if (bombDTOList == null) return new ArrayList<BombInfo>();
                    List<BombInfo> bombInfoList = new ArrayList<>();
                    for (BombDTO bombDTO : bombDTOList)
                        bombInfoList.add(bombDTO.toBombInfo());
                    return bombInfoList;
                }).exceptionally(ex -> {
                    BombSpyClient.LOGGER.error("Failed to fetch active bombs", ex);
                    return null;
                });
    }

    public static CompletableFuture<@Nullable BombInfo> putThrownBomb(BombInfo bomb) {
        BombSpyFeature bombSpyFeature = Managers.Feature.getFeatureInstance(BombSpyFeature.class);

        URI uri = URI.create("http://" + bombSpyFeature.apiAddress.get());

        Map<String, String> headers = new HashMap<>();
        headers.put("X-API-Key", bombSpyFeature.contributingApiKey.get());

        return HttpUtils.put(uri, headers, BombDTO.fromBombInfo(bomb))
                .thenApply(response -> {
                    BombDTO bombDTO = handleResponse(response, responseBody -> parseResponseBody(responseBody, BombDTO.class));
                    if (bombDTO == null) return null;
                    return bombDTO.toBombInfo();
                }).exceptionally(ex -> {
                    BombSpyClient.LOGGER.error("Failed to put thrown bomb", ex);
                    return null;
                });
    }

    private static <T> @Nullable T handleResponse(HttpResponse<String> resp, Function<String, T> on200) {
        if (resp != null && resp.statusCode() == 200) {
            BombSpyClient.LOGGER.debug("API response: {}", resp.body());
            return on200.apply(resp.body());
        } else if (resp != null) {
            BombSpyClient.LOGGER.error("API error ({}): {}", resp.statusCode(), resp.body());
        }

        return null;
    }

    private static <T> @Nullable T parseResponseBody(String responseBody, Type clazz) {
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
            return gson.fromJson(responseBody, clazz);
        } catch (Exception e) {
            BombSpyClient.LOGGER.error("Failed to parse put thrown bomb response {}", responseBody, e);
            return null;
        }
    }
}
