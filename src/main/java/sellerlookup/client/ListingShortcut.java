package sellerlookup.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jspecify.annotations.Nullable;
import sellerlookup.Seller;
import sellerlookup.SellerLookupConfig;
import sellerlookup.compat.Compat;

import java.util.List;

/**
 * In a screen such as an auction house: holding Shift (or Ctrl) over a listing for a moment closes the screen and
 * opens the chat with a command about the seller already typed, e.g. "/ah Notch". Nothing is sent: Enter does that.
 * <p>
 * The key has to be held over the same seller's listing for a little while, so a quick shift-click (or holding Shift
 * for another mod's tooltip) is left alone; a click while it is held cancels it.
 */
public final class ListingShortcut {
    /** A tooltip drawn this recently means the mouse is still over that listing. */
    private static final long HOVER_MS = 250;

    private static @Nullable String seller;
    private static long hoverStart;
    private static long hoverSeen;
    private static long shiftSince = -1;
    private static long ctrlSince = -1;
    /** Fired or cancelled during this hold; waits until the keys are let go. */
    private static boolean used;

    private ListingShortcut() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> tooltip(stack, lines));
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof AbstractContainerScreen<?>)) return;
            reset();
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, event) -> keyDown(event));
            ScreenKeyboardEvents.afterKeyRelease(screen).register((s, event) -> keyUp(event));
            ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> {
                // a click while the key is held (a shift-click) cancels this hold
                if (shiftSince >= 0 || ctrlSince >= 0) used = true;
                return true;
            });
            ScreenEvents.afterTick(screen).register(ListingShortcut::tick);
        });
    }

    private static void reset() {
        seller = null;
        shiftSince = -1;
        ctrlSince = -1;
        used = false;
    }

    /** The seller of this item, if a line under it names one. */
    static @Nullable String sellerOf(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return null;
        List<String> lines = lore.lines().stream().map(Component::getString).toList();
        String pattern = SellerLookupConfig.get().sellerPattern;
        return Seller.parse(lines, pattern.isEmpty() ? null : pattern);
    }

    private static void tooltip(ItemStack stack, List<Component> lines) {
        SellerLookupConfig config = SellerLookupConfig.get();
        if (!config.enabled || !(Compat.screen(Minecraft.getInstance()) instanceof AbstractContainerScreen<?>)) return;
        String found = sellerOf(stack);
        if (found == null) return;
        long now = System.currentTimeMillis();
        if (!found.equals(seller) || now - hoverSeen > HOVER_MS) hoverStart = now;
        seller = found;
        hoverSeen = now;
        if (!config.tooltipHint) return;
        if (!config.shiftCommand.isBlank()) {
            lines.add(Component.translatable("sellerlookup.tooltip.shift", Seller.command(config.shiftCommand, found))
                .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (!config.ctrlCommand.isBlank()) {
            lines.add(Component.translatable("sellerlookup.tooltip.ctrl", Seller.command(config.ctrlCommand, found))
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static boolean is(KeyEvent event, String left, String right) {
        int key = event.key();
        return key == InputConstants.getKey(left).getValue() || key == InputConstants.getKey(right).getValue();
    }

    private static void keyDown(KeyEvent event) {
        long now = System.currentTimeMillis();
        boolean shift = is(event, "key.keyboard.left.shift", "key.keyboard.right.shift");
        boolean ctrl = is(event, "key.keyboard.left.control", "key.keyboard.right.control");
        if (!shift && !ctrl) return;
        // a new hold starts when neither key was down
        if (shiftSince < 0 && ctrlSince < 0) used = false;
        if (shift && shiftSince < 0) shiftSince = now;
        if (ctrl && ctrlSince < 0) ctrlSince = now;
    }

    private static void keyUp(KeyEvent event) {
        if (is(event, "key.keyboard.left.shift", "key.keyboard.right.shift")) shiftSince = -1;
        if (is(event, "key.keyboard.left.control", "key.keyboard.right.control")) ctrlSince = -1;
        if (shiftSince < 0 && ctrlSince < 0) used = false;
    }

    private static void tick(Screen screen) {
        SellerLookupConfig config = SellerLookupConfig.get();
        if (used || !config.enabled || seller == null) return;
        long now = System.currentTimeMillis();
        if (now - hoverSeen > HOVER_MS) return;
        if (held(shiftSince, now, config) && !config.shiftCommand.isBlank()) {
            open(Seller.command(config.shiftCommand, seller));
        } else if (held(ctrlSince, now, config) && !config.ctrlCommand.isBlank()) {
            open(Seller.command(config.ctrlCommand, seller));
        }
    }

    /** Held long enough, counted from when the key went down or the mouse came over this seller's listing. */
    private static boolean held(long since, long now, SellerLookupConfig config) {
        return since >= 0 && now - Math.max(since, hoverStart) >= config.holdMs;
    }

    /** Closes the screen (telling the server, as closing it by hand does) and opens the chat with the command. */
    private static void open(String command) {
        used = true;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.closeContainer();
        Compat.setScreen(mc, new ChatScreen(command, false));
    }
}
