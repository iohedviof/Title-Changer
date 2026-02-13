package dev.titlechanger.titlechanger.client.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

public final class TitlechangerKeybinds {
    public static final String CATEGORY = "category.titlechanger";
    public static final String OPEN_CONFIG_KEY_TRANSLATION = "key.titlechanger.open_config";

    public static KeyMapping OPEN_CONFIG_KEY;

    private TitlechangerKeybinds() {
    }

    public static void init() {
        if (OPEN_CONFIG_KEY != null) {
            return;
        }

        KeyMapping mapping = createKeyMapping();
        if (mapping == null) {
            return;
        }
        OPEN_CONFIG_KEY = KeyBindingHelper.registerKeyBinding(mapping);
    }

    public static void applyKeyCode(InputConstants.Key keyCode) {
        if (OPEN_CONFIG_KEY == null) {
            init();
        }
        if (keyCode == null || OPEN_CONFIG_KEY == null) {
            return;
        }
        OPEN_CONFIG_KEY.setKey(keyCode);
        KeyMapping.resetMapping();
    }

    private static KeyMapping createKeyMapping() {
        Constructor<?>[] ctors = KeyMapping.class.getConstructors();
        for (Constructor<?> ctor : ctors) {
            Class<?>[] params = ctor.getParameterTypes();
            Object[] args = new Object[params.length];
            int stringCount = 0;
            boolean supported = true;

            for (int i = 0; i < params.length; i++) {
                Class<?> type = params[i];
                if (type == String.class) {
                    stringCount++;
                    args[i] = (stringCount == 1) ? OPEN_CONFIG_KEY_TRANSLATION : CATEGORY;
                } else if (type == InputConstants.Type.class) {
                    args[i] = InputConstants.Type.KEYSYM;
                } else if (type == InputConstants.Key.class) {
                    args[i] = InputConstants.UNKNOWN;
                } else if (type == int.class || type == Integer.class) {
                    args[i] = InputConstants.UNKNOWN.getValue();
                } else if (type == boolean.class || type == Boolean.class) {
                    args[i] = false;
                } else if (type == Component.class) {
                    args[i] = Component.translatable(OPEN_CONFIG_KEY_TRANSLATION);
                } else if (Supplier.class.isAssignableFrom(type)) {
                    args[i] = (Supplier<Component>) () -> Component.translatable(OPEN_CONFIG_KEY_TRANSLATION);
                } else {
                    supported = false;
                    break;
                }
            }

            if (!supported) {
                continue;
            }

            try {
                return (KeyMapping) ctor.newInstance(args);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }
}
