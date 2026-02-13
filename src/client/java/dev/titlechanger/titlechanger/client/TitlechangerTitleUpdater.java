package dev.titlechanger.titlechanger.client;

import dev.titlechanger.titlechanger.client.config.TitlechangerConfig;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.Locale;

public final class TitlechangerTitleUpdater {
    private static boolean applying = false;
    private static String temporaryTitle = null;
    private static long temporaryTitleUntilMs = 0L;

    private TitlechangerTitleUpdater() {
    }

    public static void applyTitle() {
        if (applying) {
            return;
        }

        applying = true;
        try {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }

        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableTitleChanging) {
            minecraft.updateTitle();
            return;
        }

        long now = System.currentTimeMillis();
        if (temporaryTitle != null && now < temporaryTitleUntilMs) {
            minecraft.getWindow().setTitle(temporaryTitle);
            return;
        }
        if (temporaryTitle != null && now >= temporaryTitleUntilMs) {
            temporaryTitle = null;
            temporaryTitleUntilMs = 0L;
        }

        String targetTitle = null;
        if (config.useTemplateTitles) {
            targetTitle = resolveTemplateTitle(minecraft);
        } else if (config.animationMode != TitlechangerConfig.AnimationMode.NONE
                && config.animationTitle != null
                && !config.animationTitle.isBlank()) {
            targetTitle = resolveAnimatedTitle(config);
        } else if (config.windowTitle != null && !config.windowTitle.isBlank()) {
            targetTitle = config.windowTitle;
        }

        if (config.animationMode == TitlechangerConfig.AnimationMode.TYPEWRITER
                && targetTitle != null
                && targetTitle.isEmpty()) {
            minecraft.getWindow().setTitle("");
            return;
        }

        if (targetTitle == null || targetTitle.isBlank()) {
            minecraft.updateTitle();
            return;
        }

        // Minecraft may overwrite the title after our previous tick, so always apply.
        minecraft.getWindow().setTitle(targetTitle);
        } finally {
            applying = false;
        }
    }

    public static void setTemporaryTitle(String title, long durationMs) {
        if (title == null || title.isBlank() || durationMs <= 0L) {
            return;
        }
        temporaryTitle = title;
        temporaryTitleUntilMs = System.currentTimeMillis() + durationMs;
    }

    private static String resolveTemplateTitle(Minecraft minecraft) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (config.templateUseDimensionSpecific) {
            return resolveDimensionTemplate(minecraft);
        }
        if (!config.templateUsePlayingName) {
            return null;
        }

        ServerData serverData = minecraft.getCurrentServer();
        if (serverData != null && serverData.name != null && !serverData.name.isBlank()) {
            return "Playing: " + serverData.name;
        }
        if (serverData != null && serverData.ip != null && !serverData.ip.isBlank()) {
            return "Playing: " + serverData.ip;
        }

        if (minecraft.hasSingleplayerServer()) {
            String worldName = tryGetSingleplayerWorldName(minecraft.getSingleplayerServer());
            if (worldName == null || worldName.isBlank()) {
                worldName = "Singleplayer";
            }
            return "Playing: " + worldName;
        }

        return null;
    }

    private static String resolveAnimatedTitle(TitlechangerConfig config) {
        String base = config.animationTitle == null ? "" : config.animationTitle;
        if (base.isBlank()) {
            return null;
        }

        int speed = Math.max(
                TitlechangerConfig.MIN_ANIMATION_SPEED,
                Math.min(TitlechangerConfig.MAX_ANIMATION_SPEED, config.animationSpeed));
        int slowdownFactor = (TitlechangerConfig.MAX_ANIMATION_SPEED + 1) - speed;
        long frame = (System.currentTimeMillis() / 100L) / slowdownFactor;

        return switch (config.animationMode) {
            case FADE -> (frame % 2 == 0) ? base : base.toLowerCase(Locale.ROOT);
            case PULSE -> {
                int level = (int) (frame % 4);
                String pulse = "*".repeat(level + 1);
                yield pulse + " " + base + " " + pulse;
            }
            case TYPEWRITER -> {
                int maxLen = base.length();
                int cycle = Math.max(1, maxLen * 2);
                int position = (int) (frame % cycle);
                int index = position <= maxLen ? position : (cycle - position);
                yield base.substring(0, index);
            }
            case WAVE -> {
                int index = (int) (frame % base.length());
                char[] chars = base.toCharArray();
                chars[index] = Character.toUpperCase(chars[index]);
                yield new String(chars);
            }
            case NONE -> base;
        };
    }

    private static String resolveDimensionTemplate(Minecraft minecraft) {
        if (minecraft.level == null) {
            return null;
        }

        ResourceKey<Level> dimension = minecraft.level.dimension();

        if (Level.NETHER.equals(dimension)) {
            return "Hell... or not";
        }
        if (Level.END.equals(dimension)) {
            return "THE END IS NIGH THE END IS NULL (wow tbs reference)";
        }

        return "Building... or exploring";
    }

    private static String tryGetSingleplayerWorldName(Object singleplayerServer) {
        if (singleplayerServer == null) {
            return null;
        }

        try {
            Method getWorldData = singleplayerServer.getClass().getMethod("getWorldData");
            Object worldData = getWorldData.invoke(singleplayerServer);
            if (worldData != null) {
                Method getLevelName = worldData.getClass().getMethod("getLevelName");
                Object result = getLevelName.invoke(worldData);
                if (result instanceof String value && !value.isBlank()) {
                    return value;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method getMotd = singleplayerServer.getClass().getMethod("getMotd");
            Object result = getMotd.invoke(singleplayerServer);
            if (result instanceof String value && !value.isBlank()) {
                return value;
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return null;
    }
}
