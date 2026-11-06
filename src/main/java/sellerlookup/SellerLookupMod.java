package sellerlookup;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sellerlookup.client.ListingShortcut;
import sellerlookup.client.SellerLookupScreen;
import sellerlookup.compat.Compat;

public class SellerLookupMod implements ClientModInitializer {
    public static final String MOD_ID = "sellerlookup";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyMapping.Category KEY_CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "keybindings"));
    // InputConstants.UNKNOWN is the game's "no key": -1 up to 26.2, 0 from 26.3 (SDL input). A hard-coded -1
    // counts as a real key in 26.3 and crashes the game when it reads the keyboard state.
    /** Unbound by default so it never clashes with another mod; set it in Controls. */
    public static final KeyMapping SETTINGS_KEY = new KeyMapping("key.sellerlookup.settings", InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);

    @Override
    public void onInitializeClient() {
        SellerLookupConfig.get();
        KeyMappingHelper.registerKeyMapping(SETTINGS_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (SETTINGS_KEY.consumeClick()) {
                Compat.setScreen(client, new SellerLookupScreen(Compat.screen(client)));
            }
        });
        ListingShortcut.register();
    }
}
