package dev.titlechanger.titlechanger.client.config;

public class TitlechangerConfig {
    public static final int MIN_ANIMATION_SPEED = 1;
    public static final int MAX_ANIMATION_SPEED = 10;

    public enum AnimationMode {
        NONE,
        FADE,
        PULSE,
        TYPEWRITER,
        WAVE
    }

    public enum TriggerBindMode {
        NONE,
        ON_SLEEP,
        ON_RESPAWN,
        ON_KILL
    }

    public boolean enableTitleChanging = true;
    public boolean useTemplateTitles = false;
    public boolean templateUsePlayingName = false;
    public boolean templateUseDimensionSpecific = false;
    public String windowTitle = "";
    public String openConfigKey = "";
    public boolean enableSetWindowTitleCommand = true;
    public AnimationMode animationMode = AnimationMode.NONE;
    public String animationTitle = "";
    public int animationSpeed = 6;
    public boolean triggerOnSleep = false;
    public boolean triggerOnRespawn = false;
    public boolean triggerOnKill = false;

    // Legacy single-select trigger mode kept for migration of old config files.
    public TriggerBindMode triggerBindMode = TriggerBindMode.NONE;
}
