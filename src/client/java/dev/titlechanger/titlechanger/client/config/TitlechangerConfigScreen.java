package dev.titlechanger.titlechanger.client.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.titlechanger.titlechanger.client.TitlechangerTitleUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class TitlechangerConfigScreen {
    private TitlechangerConfigScreen() {
    }

    public static Screen create(Screen parent) {
        TitlechangerConfig config = TitlechangerConfigManager.get();

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Title Changer"))
                .save(() -> {
                    TitlechangerConfigManager.save();
                    TitlechangerTitleUpdater.applyTitle();
                });

        Option<Boolean> enableTitleChangingOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Enable Title Changing"))
                .description(OptionDescription.of(
                        Component.literal("Boolean toggle to the mod. Turning it off basically makes the mod unactive.")
                ))
                .binding(true, () -> config.enableTitleChanging, value -> config.enableTitleChanging = value)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> useTemplateTitlesOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Use Template Titles"))
                .description(OptionDescription.of(
                        Component.literal("Enables template title mode and disables manual Window Title.")
                ))
                .binding(false, () -> config.useTemplateTitles, value -> config.useTemplateTitles = value)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> playingNameTemplateOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Template: Playing <name>"))
                .description(OptionDescription.of(
                        Component.literal("Uses \"Playing: <server/world name>\".")
                ))
                .binding(false, () -> config.templateUsePlayingName, value -> {
                    config.templateUsePlayingName = value;
                    if (value) {
                        config.templateUseDimensionSpecific = false;
                    }
                })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> dimensionTemplateOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Template: Dimension Specific"))
                .description(OptionDescription.of(
                        Component.literal("Overworld: \"Building... or exploring\"; Nether: \"Hell... or not\"; End: \"THE END IS NIGH THE END IS NULL (wow tbs reference)\".")
                ))
                .binding(false, () -> config.templateUseDimensionSpecific, value -> {
                    config.templateUseDimensionSpecific = value;
                    if (value) {
                        config.templateUsePlayingName = false;
                    }
                })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<String> windowTitleOption = Option.<String>createBuilder()
                .name(Component.literal("Window Title"))
                .description(OptionDescription.createBuilder()
                        .text(Component.literal("Your desired window title (e.g. Alex's Minecraft Instance* 1.21.11). Also, sorry for the slightly pixelated picture example on top of this description :("))
                        .image(ResourceLocation.parse("titlechanger:textures/gui/example.png"), 128, 128)
                        .build())
                .binding("", () -> config.windowTitle, value -> config.windowTitle = value)
                .controller(StringControllerBuilder::create)
                .build();

        Runnable refreshAvailability = () -> {
            windowTitleOption.setAvailable(!config.useTemplateTitles);
            playingNameTemplateOption.setAvailable(config.useTemplateTitles && !config.templateUseDimensionSpecific);
            dimensionTemplateOption.setAvailable(config.useTemplateTitles && !config.templateUsePlayingName);
        };

        useTemplateTitlesOption.addListener((opt, value) -> refreshAvailability.run());
        playingNameTemplateOption.addListener((opt, value) -> refreshAvailability.run());
        dimensionTemplateOption.addListener((opt, value) -> refreshAvailability.run());
        refreshAvailability.run();

        OptionGroup templateGroup = OptionGroup.createBuilder()
                .name(Component.literal("Template Titles"))
                .description(OptionDescription.of(
                        Component.literal("Template title modes for multiplayer/singleplayer and per-dimension titles.")
                ))
                .option(useTemplateTitlesOption)
                .option(playingNameTemplateOption)
                .option(dimensionTemplateOption)
                .build();

        ConfigCategory general = ConfigCategory.createBuilder()
                .name(Component.literal("General"))
                .option(enableTitleChangingOption)
                .group(templateGroup)
                .option(windowTitleOption)
                .build();

        builder.category(general);

        Option<Boolean> enableAliasOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Toggle /titlechanger Alias"))
                .description(OptionDescription.of(
                        Component.literal("Toggles the /titlechanger command alias.")
                ))
                .binding(true, () -> config.enableSetWindowTitleCommand, value -> config.enableSetWindowTitleCommand = value)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> animationFadeOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Animation: Fade"))
                .description(OptionDescription.of(Component.literal("Uses the Fade animation mode.")))
                .binding(false,
                        () -> config.animationMode == TitlechangerConfig.AnimationMode.FADE,
                        value -> {
                            if (value) {
                                config.animationMode = TitlechangerConfig.AnimationMode.FADE;
                            } else if (config.animationMode == TitlechangerConfig.AnimationMode.FADE) {
                                config.animationMode = TitlechangerConfig.AnimationMode.NONE;
                            }
                        })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> animationPulseOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Animation: Pulse"))
                .description(OptionDescription.of(Component.literal("Uses the Pulse animation mode.")))
                .binding(false,
                        () -> config.animationMode == TitlechangerConfig.AnimationMode.PULSE,
                        value -> {
                            if (value) {
                                config.animationMode = TitlechangerConfig.AnimationMode.PULSE;
                            } else if (config.animationMode == TitlechangerConfig.AnimationMode.PULSE) {
                                config.animationMode = TitlechangerConfig.AnimationMode.NONE;
                            }
                        })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> animationTypewriterOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Animation: Typewriter"))
                .description(OptionDescription.of(Component.literal("Uses the Typewriter animation mode.")))
                .binding(false,
                        () -> config.animationMode == TitlechangerConfig.AnimationMode.TYPEWRITER,
                        value -> {
                            if (value) {
                                config.animationMode = TitlechangerConfig.AnimationMode.TYPEWRITER;
                            } else if (config.animationMode == TitlechangerConfig.AnimationMode.TYPEWRITER) {
                                config.animationMode = TitlechangerConfig.AnimationMode.NONE;
                            }
                        })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> animationWaveOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Animation: Wave"))
                .description(OptionDescription.of(Component.literal("Uses the Wave animation mode.")))
                .binding(false,
                        () -> config.animationMode == TitlechangerConfig.AnimationMode.WAVE,
                        value -> {
                            if (value) {
                                config.animationMode = TitlechangerConfig.AnimationMode.WAVE;
                            } else if (config.animationMode == TitlechangerConfig.AnimationMode.WAVE) {
                                config.animationMode = TitlechangerConfig.AnimationMode.NONE;
                            }
                        })
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<String> animationTitleOption = Option.<String>createBuilder()
                .name(Component.literal("Animation Title"))
                .description(OptionDescription.of(Component.literal("Title text used by animation modes.")))
                .binding("", () -> config.animationTitle, value -> config.animationTitle = value)
                .controller(StringControllerBuilder::create)
                .build();

        Option<String> animationSpeedOption = Option.<String>createBuilder()
                .name(Component.literal("Animation Speed"))
                .description(OptionDescription.of(Component.literal("Animation speed (" + TitlechangerConfig.MIN_ANIMATION_SPEED + " to " + TitlechangerConfig.MAX_ANIMATION_SPEED + ").")))
                .binding("6", () -> Integer.toString(config.animationSpeed), value -> {
                    try {
                        int parsed = Integer.parseInt(value.trim());
                        config.animationSpeed = Math.max(
                                TitlechangerConfig.MIN_ANIMATION_SPEED,
                                Math.min(TitlechangerConfig.MAX_ANIMATION_SPEED, parsed));
                    } catch (NumberFormatException ignored) {
                    }
                })
                .controller(StringControllerBuilder::create)
                .build();

        Option<Boolean> triggerSleepOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Bind Trigger: on_sleep"))
                .description(OptionDescription.of(Component.literal("Sets trigger bind mode to on_sleep.")))
                .binding(false, () -> config.triggerOnSleep, value -> config.triggerOnSleep = value)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> triggerRespawnOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Bind Trigger: on_respawn"))
                .description(OptionDescription.of(Component.literal("Sets trigger bind mode to on_respawn.")))
                .binding(false, () -> config.triggerOnRespawn, value -> config.triggerOnRespawn = value)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Option<Boolean> triggerKillOption = Option.<Boolean>createBuilder()
                .name(Component.literal("Bind Trigger: on_death"))
                .description(OptionDescription.of(Component.literal("Sets trigger bind mode to on_death.")))
                .binding(false, () -> config.triggerOnKill, value -> config.triggerOnKill = value)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(value -> Component.literal(value ? "True" : "False"))
                        .coloured(true))
                .build();

        Runnable refreshCommandAvailability = () -> {
            boolean commandEnabled = config.enableSetWindowTitleCommand;
            animationFadeOption.setAvailable(commandEnabled && config.animationMode != TitlechangerConfig.AnimationMode.PULSE
                    && config.animationMode != TitlechangerConfig.AnimationMode.TYPEWRITER
                    && config.animationMode != TitlechangerConfig.AnimationMode.WAVE);
            animationPulseOption.setAvailable(commandEnabled && config.animationMode != TitlechangerConfig.AnimationMode.FADE
                    && config.animationMode != TitlechangerConfig.AnimationMode.TYPEWRITER
                    && config.animationMode != TitlechangerConfig.AnimationMode.WAVE);
            animationTypewriterOption.setAvailable(commandEnabled && config.animationMode != TitlechangerConfig.AnimationMode.FADE
                    && config.animationMode != TitlechangerConfig.AnimationMode.PULSE
                    && config.animationMode != TitlechangerConfig.AnimationMode.WAVE);
            animationWaveOption.setAvailable(commandEnabled && config.animationMode != TitlechangerConfig.AnimationMode.FADE
                    && config.animationMode != TitlechangerConfig.AnimationMode.PULSE
                    && config.animationMode != TitlechangerConfig.AnimationMode.TYPEWRITER);
            animationTitleOption.setAvailable(commandEnabled);
            animationSpeedOption.setAvailable(commandEnabled);
            triggerSleepOption.setAvailable(commandEnabled);
            triggerRespawnOption.setAvailable(commandEnabled);
            triggerKillOption.setAvailable(commandEnabled);
        };

        enableAliasOption.addListener((opt, value) -> refreshCommandAvailability.run());
        animationFadeOption.addListener((opt, value) -> refreshCommandAvailability.run());
        animationPulseOption.addListener((opt, value) -> refreshCommandAvailability.run());
        animationTypewriterOption.addListener((opt, value) -> refreshCommandAvailability.run());
        animationWaveOption.addListener((opt, value) -> refreshCommandAvailability.run());
        triggerSleepOption.addListener((opt, value) -> refreshCommandAvailability.run());
        triggerRespawnOption.addListener((opt, value) -> refreshCommandAvailability.run());
        triggerKillOption.addListener((opt, value) -> refreshCommandAvailability.run());
        refreshCommandAvailability.run();

        OptionGroup aliasGroup = OptionGroup.createBuilder()
                .name(Component.literal("Alias"))
                .description(OptionDescription.of(Component.literal("Main /titlechanger alias controls.")))
                .option(enableAliasOption)
                .build();

        OptionGroup animationGroup = OptionGroup.createBuilder()
                .name(Component.literal("Animation"))
                .description(OptionDescription.of(Component.literal("Animation command settings.")))
                .option(animationFadeOption)
                .option(animationPulseOption)
                .option(animationTypewriterOption)
                .option(animationWaveOption)
                .option(animationTitleOption)
                .option(animationSpeedOption)
                .build();

        OptionGroup triggerGroup = OptionGroup.createBuilder()
                .name(Component.literal("Trigger Binds"))
                .description(OptionDescription.of(Component.literal("Trigger bind command settings.")))
                .option(triggerSleepOption)
                .option(triggerRespawnOption)
                .option(triggerKillOption)
                .build();

        ConfigCategory commandAliases = ConfigCategory.createBuilder()
                .name(Component.literal("Command Aliases"))
                .group(aliasGroup)
                .group(animationGroup)
                .group(triggerGroup)
                .build();

        builder.category(commandAliases);

        return builder.build().generateScreen(parent);
    }

    public static void open() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(create(client.screen));
    }
}
