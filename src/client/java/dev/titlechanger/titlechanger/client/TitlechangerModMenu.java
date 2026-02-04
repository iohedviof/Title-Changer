package dev.titlechanger.titlechanger.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.titlechanger.titlechanger.client.config.TitlechangerConfigScreen;

public class TitlechangerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TitlechangerConfigScreen::create;
    }
}
