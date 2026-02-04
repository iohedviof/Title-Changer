package dev.titlechanger.titlechanger.mixin.client;

import dev.titlechanger.titlechanger.client.TitlechangerTitleUpdater;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfig;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "updateTitle", at = @At("TAIL"))
    private void titlechanger$updateTitle(CallbackInfo ci) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (config.enableTitleChanging && config.windowTitle != null && !config.windowTitle.isBlank()) {
            TitlechangerTitleUpdater.applyTitle();
        }
    }
}
