package dev.titlechanger.titlechanger.client;

import dev.titlechanger.titlechanger.client.config.TitlechangerConfig;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigManager;
import net.minecraft.client.Minecraft;

public final class TitlechangerTitleUpdater {
    private TitlechangerTitleUpdater() {
    }

    public static void applyTitle() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }

        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (config.enableTitleChanging && config.windowTitle != null && !config.windowTitle.isBlank()) {
            minecraft.getWindow().setTitle(config.windowTitle);
        } else {
            minecraft.updateTitle();
        }
    }
}
