package dev.titlechanger.titlechanger.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import dev.titlechanger.titlechanger.client.TitlechangerTitleUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TitlechangerConfigScreen {
    private TitlechangerConfigScreen() {
    }

    public static Screen create(Screen parent) {
        TitlechangerConfig config = TitlechangerConfigManager.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Title Changer"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.literal("Enable Title Changing"),
                        config.enableTitleChanging)
                .setDefaultValue(false)
                .setSaveConsumer(value -> config.enableTitleChanging = value)
                .build());

        general.addEntry(entryBuilder.startStrField(
                        Component.literal("Window Title"),
                        config.windowTitle)
                .setDefaultValue("")
                .setSaveConsumer(value -> config.windowTitle = value)
                .build());

        ModifierKeyCode defaultKey = ModifierKeyCode.unknown();
        general.addEntry(entryBuilder.startModifierKeyCodeField(
                        Component.literal("Open Config Keybind"),
                        TitlechangerConfigManager.getOpenConfigKeyCode())
                .setDefaultValue(defaultKey)
                .setModifierSaveConsumer(value -> {
                    TitlechangerConfigManager.setOpenConfigKeyCode(value);
                    TitlechangerKeybinds.applyKeyCode(value);
                })
                .build());

        builder.setSavingRunnable(() -> {
            TitlechangerConfigManager.save();
            TitlechangerTitleUpdater.applyTitle();
        });

        return builder.build();
    }

    public static void open() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(create(client.screen));
    }
}
