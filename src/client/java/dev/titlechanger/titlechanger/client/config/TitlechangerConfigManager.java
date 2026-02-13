package dev.titlechanger.titlechanger.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TitlechangerConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("titlechanger.json");
    private static TitlechangerConfig config = new TitlechangerConfig();

    private TitlechangerConfigManager() {
    }

    public static TitlechangerConfig get() {
        return config;
    }

    public static InputConstants.Key getOpenConfigKey() {
        String storedKey = config.openConfigKey == null ? "" : config.openConfigKey;
        if (storedKey.isBlank()) {
            return InputConstants.UNKNOWN;
        }
        try {
            return InputConstants.getKey(storedKey);
        } catch (Exception ignored) {
            return InputConstants.UNKNOWN;
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            TitlechangerConfig loaded = GSON.fromJson(reader, TitlechangerConfig.class);
            if (loaded != null) {
                config = loaded;
                if (config.openConfigKey == null) {
                    config.openConfigKey = "";
                }
                if (config.animationMode == null) {
                    config.animationMode = TitlechangerConfig.AnimationMode.NONE;
                }
                if (config.animationTitle == null) {
                    config.animationTitle = "";
                }
                config.animationSpeed = Math.max(
                        TitlechangerConfig.MIN_ANIMATION_SPEED,
                        Math.min(TitlechangerConfig.MAX_ANIMATION_SPEED, config.animationSpeed));
                if (config.triggerBindMode == null) {
                    config.triggerBindMode = TitlechangerConfig.TriggerBindMode.NONE;
                }
                if (!config.triggerOnSleep && !config.triggerOnRespawn && !config.triggerOnKill) {
                    if (config.triggerBindMode == TitlechangerConfig.TriggerBindMode.ON_SLEEP) {
                        config.triggerOnSleep = true;
                    } else if (config.triggerBindMode == TitlechangerConfig.TriggerBindMode.ON_RESPAWN) {
                        config.triggerOnRespawn = true;
                    } else if (config.triggerBindMode == TitlechangerConfig.TriggerBindMode.ON_KILL) {
                        config.triggerOnKill = true;
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
        } catch (IOException ignored) {
        }

        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException ignored) {
        }
    }
}
