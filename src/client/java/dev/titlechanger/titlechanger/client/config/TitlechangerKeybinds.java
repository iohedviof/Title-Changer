package dev.titlechanger.titlechanger.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public final class TitlechangerKeybinds {
    public static final String CATEGORY = "category.titlechanger";
    public static final String OPEN_CONFIG_KEY_TRANSLATION = "key.titlechanger.open_config";

    public static final KeyMapping OPEN_CONFIG_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    OPEN_CONFIG_KEY_TRANSLATION,
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    CATEGORY
            )
    );

    private TitlechangerKeybinds() {
    }

    public static void applyKeyCode(ModifierKeyCode keyCode) {
        if (keyCode == null) {
            return;
        }
        OPEN_CONFIG_KEY.setKey(keyCode.getKeyCode());
        KeyMapping.resetMapping();
    }
}
