package dev.titlechanger.titlechanger.client;

import dev.titlechanger.titlechanger.client.config.TitlechangerConfigManager;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigScreen;
import dev.titlechanger.titlechanger.client.config.TitlechangerKeybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class TitlechangerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TitlechangerConfigManager.load();
        TitlechangerKeybinds.applyKeyCode(TitlechangerConfigManager.getOpenConfigKeyCode());
        TitlechangerTitleUpdater.applyTitle();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TitlechangerKeybinds.OPEN_CONFIG_KEY.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(TitlechangerConfigScreen.create(minecraft.screen));
            }
        });
    }
}
