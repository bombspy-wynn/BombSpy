package xyz.bombspy.client.mixin;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.FeatureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bombspy.client.features.BombSpyFeature;

@Mixin(FeatureManager.class)
public class FeatureManagerMixin {
    @Shadow
    private void registerFeature(Feature feature) {}

    @Inject(method = "init", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        registerFeature(new BombSpyFeature());
    }
}
