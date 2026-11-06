package sellerlookup.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import sellerlookup.client.SellerLookupScreen;

/** Opens the settings from Mod Menu's "Configure" button. Only loaded when Mod Menu is installed. */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SellerLookupScreen::new;
    }
}
