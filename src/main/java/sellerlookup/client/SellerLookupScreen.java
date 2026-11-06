package sellerlookup.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import sellerlookup.SellerLookupConfig;
import sellerlookup.client.ui.SettingsList;
import sellerlookup.client.ui.SettingsScreen;

import java.util.List;
import java.util.function.Consumer;

/** On/off, how long to hold, and the command for Shift and for Ctrl. */
public class SellerLookupScreen extends SettingsScreen {
    private final SellerLookupConfig config = SellerLookupConfig.get();

    public SellerLookupScreen(@Nullable Screen parent) {
        super(Component.translatable("sellerlookup.screen.title"), parent);
    }

    @Override
    protected void buildList(SettingsList list) {
        list.header(Component.translatable("sellerlookup.section.general"));
        list.row(
            list.checkbox(Component.translatable("sellerlookup.screen.enabled"), config.enabled, v -> config.enabled = v,
                Component.translatable("sellerlookup.screen.enabled.tooltip")),
            list.checkbox(Component.translatable("sellerlookup.screen.hint"), config.tooltipHint,
                v -> config.tooltipHint = v, Component.translatable("sellerlookup.screen.hint.tooltip")));
        list.row(new HoldSlider());

        list.header(Component.translatable("sellerlookup.section.shift"));
        list.row(commandBox(config.shiftCommand, v -> config.shiftCommand = v));
        list.header(Component.translatable("sellerlookup.section.ctrl"));
        list.row(commandBox(config.ctrlCommand, v -> config.ctrlCommand = v));
    }

    private EditBox commandBox(String value, Consumer<String> setter) {
        EditBox box = new EditBox(this.font, 0, 0, SettingsList.ROW_WIDTH, 20, Component.translatable("sellerlookup.screen.command"));
        box.setMaxLength(256);
        box.setValue(value);
        box.setResponder(v -> setter.accept(v.trim()));
        box.setTooltip(Tooltip.create(Component.translatable("sellerlookup.screen.command.tooltip")));
        return box;
    }

    @Override
    protected List<Component> infoLines() {
        return List.of(Component.translatable("sellerlookup.screen.info"));
    }

    @Override
    protected void save() {
        config.save();
    }

    @Override
    protected Component resetLabel() {
        return Component.translatable("sellerlookup.screen.reset");
    }

    @Override
    protected Component resetQuestion() {
        return Component.translatable("sellerlookup.screen.reset.title");
    }

    @Override
    protected Component resetWarning() {
        return Component.translatable("sellerlookup.screen.reset.message");
    }

    @Override
    protected void reset() {
        config.resetToDefaults();
    }

    /** 0 to 1.5 seconds, in steps of 50 ms. */
    private final class HoldSlider extends AbstractSliderButton {
        HoldSlider() {
            super(0, 0, SettingsList.ROW_WIDTH, 20, Component.empty(), config.holdMs / (double) SellerLookupConfig.HOLD_MAX);
            setTooltip(Tooltip.create(Component.translatable("sellerlookup.screen.hold.tooltip")));
            updateMessage();
        }

        private int ms() {
            return (int) Math.round(value * SellerLookupConfig.HOLD_MAX / 50) * 50;
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable("sellerlookup.screen.hold", ms()));
        }

        @Override
        protected void applyValue() {
            config.holdMs = ms();
        }
    }
}
