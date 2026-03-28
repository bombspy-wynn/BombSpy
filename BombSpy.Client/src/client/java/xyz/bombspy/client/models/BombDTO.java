package xyz.bombspy.client.models;

import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;

import java.time.Instant;

public record BombDTO(String thrower, BombType type, String server, Instant startTime) {
    public static BombDTO fromBombInfo(BombInfo bombInfo) {
        return new BombDTO(bombInfo.user(), bombInfo.bomb(), bombInfo.server(), Instant.ofEpochMilli(bombInfo.startTime()));
    }

    public BombInfo toBombInfo() {
        return new BombInfo(thrower, type, server, startTime.toEpochMilli(), type.getActiveMinutes());
    }
}
