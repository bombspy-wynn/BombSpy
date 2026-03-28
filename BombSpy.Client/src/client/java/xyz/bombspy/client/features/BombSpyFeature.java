package xyz.bombspy.client.features;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.ProfileDefault;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Category;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.ConfigCategory;
import com.wynntils.models.worlds.event.BombEvent;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.text.Text;
import net.neoforged.bus.api.SubscribeEvent;
import org.jspecify.annotations.Nullable;
import xyz.bombspy.client.api.BombSpyApi;
import xyz.bombspy.client.api.BombWebSocket;
import xyz.bombspy.client.core.BombSpyClient;
import xyz.bombspy.client.mixinterfaces.IBombModelMixin;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutionException;

@ConfigCategory(Category.OVERLAYS)
public class BombSpyFeature extends Feature {
    @Persisted
    public final Config<String> apiAddress = new Config<>("localhost:5000");

    @Persisted
    public final Config<Boolean> contributing = new Config<>(false);
    @Persisted
    public final Config<String> contributingApiKey = new Config<>("");


    private @Nullable BombWebSocket webSocket;

    public BombSpyFeature() {
        super(ProfileDefault.ENABLED);
    }

    @Override
    public void onEnable() {
        List<BombInfo> activeBombs;
        try {
            activeBombs = BombSpyApi.getActiveBombs().get();
        } catch (Exception e) {
            BombSpyClient.LOGGER.error("Failed to fetch active bombs: ", e);
            McUtils.sendErrorToClient("[BombSpy] Failed to fetch active bombs. Feature disabled. Check API Address and try again.");
            Managers.Feature.disableFeature(this, true);
            return;
        }

        for (BombInfo bomb : activeBombs)
            ((IBombModelMixin) (Object) Models.Bomb).addBombInfoFromBombSpy(bomb);

        createBombWebSocket();
    }

    private void createBombWebSocket() {
        if (webSocket != null) webSocket.close();
        webSocket = new BombWebSocket(URI.create("ws://" + apiAddress.get()), this::onBombWebSocketMessage, this::onBombWebSocketError, this::onBombWebSocketClose);
        webSocket.connect();
    }

    private void onBombWebSocketMessage(BombInfo bomb) {
        ((IBombModelMixin) (Object) Models.Bomb).addBombInfoFromBombSpy(bomb);
    }

    private void onBombWebSocketError(Exception e) {
        BombSpyClient.LOGGER.error("Failed to connect websocket: ", e);
        McUtils.sendErrorToClient("[BombSpy] Failed to connect websocket. Feature disabled. Check API Address and try again.");
        Managers.Feature.disableFeature(this, true);
    }

    private void onBombWebSocketClose() {
        createBombWebSocket();
    }

    @SubscribeEvent
    public void onLocalBombEvent(BombEvent.Local localBombEvent) {
        onBombEvent(localBombEvent);
    }

    @SubscribeEvent
    public void onBombBellEvent(BombEvent.BombBell bombBellEvent) {
        onBombEvent(bombBellEvent);
    }

    private void onBombEvent(BombEvent event) {
        if (!contributing.get()) return;
        if (contributingApiKey.get().isEmpty()) return;

        // This shit came from an infobar.
        // Happens every 8 seconds while a bomb is active.
        if (event.getMessage() == null) return;

        BombSpyApi.putThrownBomb(event.getBombInfo());
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        // Reconstruct the websocket if it exists
        if (config.getFieldName().equals("apiAddress") && webSocket != null) createBombWebSocket();
    }

    @Override
    public void onDisable() {
        if (webSocket == null) return;
        webSocket.close();
        webSocket = null;
    }
}
