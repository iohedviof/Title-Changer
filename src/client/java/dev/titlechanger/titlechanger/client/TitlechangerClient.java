package dev.titlechanger.titlechanger.client;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigManager;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigScreen;
import dev.titlechanger.titlechanger.client.config.TitlechangerKeybinds;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class TitlechangerClient implements ClientModInitializer {
    private static boolean openConfigRequested = false;
    private static boolean hadPlayerLastTick = false;
    private static boolean lastSleeping = false;
    private static boolean lastAlive = true;

    @Override
    public void onInitializeClient() {
        TitlechangerConfigManager.load();
        TitlechangerKeybinds.init();
        TitlechangerKeybinds.applyKeyCode(TitlechangerConfigManager.getOpenConfigKey());
        TitlechangerTitleUpdater.applyTitle();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("titlechanger")
                    .then(literal("config")
                            .executes(context -> openConfig(context.getSource())))
                    .then(literal("title")
                            .then(literal("clear")
                                    .executes(context -> clearManualTitle(context.getSource())))
                            .then(argument("title", greedyString())
                                    .executes(context -> setManualTitle(context.getSource(), getString(context, "title")))))
                    .then(literal("animate")
                            .then(argument("mode", word())
                                    .suggests((context, builder) -> suggestValues(builder, "fade", "pulse", "typewriter", "wave"))
                                    .then(argument("title", greedyString())
                                            .executes(context -> setAnimation(context.getSource(),
                                                    getString(context, "mode"),
                                                    getString(context, "title"))))))
                    .then(literal("animatespeed")
                            .then(argument("speed", integer(TitlechangerConfig.MIN_ANIMATION_SPEED, TitlechangerConfig.MAX_ANIMATION_SPEED))
                                    .executes(context -> setAnimationSpeed(context.getSource(),
                                            getInteger(context, "speed")))))
                    .then(literal("bind")
                            .then(argument("trigger", word())
                                    .suggests((context, builder) -> suggestValues(builder, "on_sleep", "on_respawn", "on_death"))
                                    .executes(context -> setTriggerBind(context.getSource(),
                                            getString(context, "trigger"))))));

            dispatcher.register(literal("setwindowtitle")
                    .then(literal("clear")
                            .executes(context -> {
                                return clearManualTitle(context.getSource());
                            }))
                    .then(argument("title", greedyString())
                            .executes(context -> {
                                return setManualTitle(context.getSource(), getString(context, "title"));
                            })));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            applyTriggerBindTitle(client);
            TitlechangerTitleUpdater.applyTitle();
            if (openConfigRequested) {
                openConfigRequested = false;
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(TitlechangerConfigScreen.create(minecraft.screen));
            }
            while (TitlechangerKeybinds.OPEN_CONFIG_KEY != null
                    && TitlechangerKeybinds.OPEN_CONFIG_KEY.consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                minecraft.setScreen(TitlechangerConfigScreen.create(minecraft.screen));
            }
        });
    }

    private static void applyTriggerBindTitle(Minecraft client) {
        if (client.player == null) {
            hadPlayerLastTick = false;
            lastSleeping = false;
            lastAlive = true;
            return;
        }

        boolean sleepingNow = client.player.isSleeping();
        boolean aliveNow = client.player.isAlive();

        if (!hadPlayerLastTick) {
            hadPlayerLastTick = true;
            lastSleeping = sleepingNow;
            lastAlive = aliveNow;
            return;
        }

        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableTitleChanging
                || (!config.triggerOnSleep && !config.triggerOnRespawn && !config.triggerOnKill)) {
            lastSleeping = sleepingNow;
            lastAlive = aliveNow;
            return;
        }

        if (config.triggerOnSleep
                && !lastSleeping
                && sleepingNow) {
            TitlechangerTitleUpdater.setTemporaryTitle("Good night!", 2000L);
        } else if (config.triggerOnKill
                && lastAlive
                && !aliveNow) {
            TitlechangerTitleUpdater.setTemporaryTitle("Ouch...", 2000L);
        } else if (config.triggerOnRespawn
                && !lastAlive
                && aliveNow) {
            TitlechangerTitleUpdater.setTemporaryTitle("Welcome back :D", 5000L);
        }

        lastSleeping = sleepingNow;
        lastAlive = aliveNow;
    }

    private static int clearManualTitle(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableSetWindowTitleCommand) {
            source.sendError(Component.literal("The /titlechanger alias is disabled."));
            return 0;
        }
        if (config.useTemplateTitles) {
            source.sendError(Component.literal("[ERROR] Templates are enabled, you can't change the title by yourself"));
            return 0;
        }
        config.windowTitle = "";
        config.enableTitleChanging = false;
        config.animationMode = TitlechangerConfig.AnimationMode.NONE;
        config.animationTitle = "";
        TitlechangerConfigManager.save();
        TitlechangerTitleUpdater.applyTitle();
        source.sendFeedback(Component.literal("Window title cleared."));
        return 1;
    }

    private static int setManualTitle(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source, String title) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableSetWindowTitleCommand) {
            source.sendError(Component.literal("The /titlechanger alias is disabled."));
            return 0;
        }
        if (config.useTemplateTitles) {
            source.sendError(Component.literal("[ERROR] Templates are enabled, you can't change the title by yourself"));
            return 0;
        }
        config.animationMode = TitlechangerConfig.AnimationMode.NONE;
        config.animationTitle = "";
        config.windowTitle = title;
        config.enableTitleChanging = true;
        TitlechangerConfigManager.save();
        TitlechangerTitleUpdater.applyTitle();
        source.sendFeedback(Component.literal("Window title set to: " + title));
        return 1;
    }

    private static int setAnimation(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source,
                                    String rawMode,
                                    String title) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableSetWindowTitleCommand) {
            source.sendError(Component.literal("The /titlechanger alias is disabled."));
            return 0;
        }
        if (config.useTemplateTitles) {
            source.sendError(Component.literal("[ERROR] Templates are enabled, you can't change the title by yourself"));
            return 0;
        }

        TitlechangerConfig.AnimationMode mode;
        switch (rawMode.toLowerCase()) {
            case "fade" -> mode = TitlechangerConfig.AnimationMode.FADE;
            case "pulse" -> mode = TitlechangerConfig.AnimationMode.PULSE;
            case "typewriter" -> mode = TitlechangerConfig.AnimationMode.TYPEWRITER;
            case "wave" -> mode = TitlechangerConfig.AnimationMode.WAVE;
            default -> {
                source.sendError(Component.literal("Unknown animation mode. Use: fade, pulse, typewriter, wave"));
                return 0;
            }
        }

        config.animationMode = mode;
        config.animationTitle = title;
        config.windowTitle = "";
        config.enableTitleChanging = true;
        TitlechangerConfigManager.save();
        TitlechangerTitleUpdater.applyTitle();
        source.sendFeedback(Component.literal("Animation set to " + rawMode + " with title: " + title));
        return 1;
    }

    private static int setAnimationSpeed(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source,
                                         int speed) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableSetWindowTitleCommand) {
            source.sendError(Component.literal("The /titlechanger alias is disabled."));
            return 0;
        }

        config.animationSpeed = Math.max(
                TitlechangerConfig.MIN_ANIMATION_SPEED,
                Math.min(TitlechangerConfig.MAX_ANIMATION_SPEED, speed));
        TitlechangerConfigManager.save();
        TitlechangerTitleUpdater.applyTitle();
        source.sendFeedback(Component.literal("Animation speed set to: " + config.animationSpeed));
        return 1;
    }

    private static int setTriggerBind(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source,
                                      String rawTrigger) {
        TitlechangerConfig config = TitlechangerConfigManager.get();
        if (!config.enableSetWindowTitleCommand) {
            source.sendError(Component.literal("The /titlechanger alias is disabled."));
            return 0;
        }

        switch (rawTrigger.toLowerCase()) {
            case "on_sleep" -> config.triggerOnSleep = !config.triggerOnSleep;
            case "on_respawn" -> config.triggerOnRespawn = !config.triggerOnRespawn;
            case "on_death" -> config.triggerOnKill = !config.triggerOnKill;
            default -> {
                source.sendError(Component.literal("Unknown trigger bind. Use: on_sleep, on_respawn, on_death"));
                return 0;
            }
        }

        TitlechangerConfigManager.save();
        source.sendFeedback(Component.literal("Trigger bind toggled: " + rawTrigger));
        return 1;
    }

    private static int openConfig(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
        openConfigRequested = true;
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestValues(SuggestionsBuilder builder, String... values) {
        for (String value : values) {
            if (value.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(value);
            }
        }
        return CompletableFuture.completedFuture(builder.build());
    }
}
