package dev.titlechanger.titlechanger.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.Modifier;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
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

    public static ModifierKeyCode getOpenConfigKeyCode() {
        String storedKey = config.openConfigKey == null ? "" : config.openConfigKey;
        if (storedKey.isBlank()) {
            return ModifierKeyCode.unknown();
        }
        try {
            return ModifierKeyCode.of(InputConstants.getKey(storedKey), Modifier.of(config.openConfigModifier));
        } catch (Exception ignored) {
            return ModifierKeyCode.unknown();
        }
    }

    public static void setOpenConfigKeyCode(ModifierKeyCode keyCode) {
        if (keyCode == null) {
            config.openConfigKey = "";
            config.openConfigModifier = 0;
            return;
        }
        config.openConfigKey = keyCode.getKeyCode().getName();
        config.openConfigModifier = keyCode.getModifier().getValue();
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
