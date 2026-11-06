package sellerlookup;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Settings, stored at config/sellerlookup.properties. */
public final class SellerLookupConfig {
    public static final int HOLD_MAX = 1500;

    public boolean enabled = true;
    /** Show what holding Shift or Ctrl would type, under the item. */
    public boolean tooltipHint = true;
    /** How long Shift or Ctrl has to be held over a listing, so that a quick shift-click still works. */
    public int holdMs = 400;
    /** Typed into the chat; {seller} becomes the seller's name. Empty turns the key off. */
    public String shiftCommand = "/ah {seller}";
    public String ctrlCommand = "/stats {seller}";
    /** A regular expression whose first group is the seller, for servers the loose matching gets wrong. */
    public String sellerPattern = "";

    private static SellerLookupConfig instance;

    public static SellerLookupConfig get() {
        if (instance == null) {
            instance = new SellerLookupConfig();
            instance.load();
        }
        return instance;
    }

    public void resetToDefaults() {
        SellerLookupConfig d = new SellerLookupConfig();
        enabled = d.enabled;
        tooltipHint = d.tooltipHint;
        holdMs = d.holdMs;
        shiftCommand = d.shiftCommand;
        ctrlCommand = d.ctrlCommand;
        sellerPattern = d.sellerPattern;
    }

    /** Only for tests: config folder to use instead of the game's. */
    static Path testConfigDir;

    private static Path file() {
        Path dir = testConfigDir != null ? testConfigDir : FabricLoader.getInstance().getConfigDir();
        return dir.resolve("sellerlookup.properties");
    }

    public void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            p.load(reader);
        } catch (IOException e) {
            SellerLookupMod.LOGGER.warn("Could not read {}", path, e);
            return;
        }
        enabled = Boolean.parseBoolean(p.getProperty("enabled", "true").trim());
        tooltipHint = Boolean.parseBoolean(p.getProperty("tooltipHint", "true").trim());
        try {
            holdMs = Math.clamp(Integer.parseInt(p.getProperty("holdMs", "400").trim()), 0, HOLD_MAX);
        } catch (NumberFormatException ignored) {
        }
        shiftCommand = p.getProperty("shiftCommand", shiftCommand).trim();
        ctrlCommand = p.getProperty("ctrlCommand", ctrlCommand).trim();
        sellerPattern = p.getProperty("sellerPattern", "").trim();
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("enabled", Boolean.toString(enabled));
        p.setProperty("tooltipHint", Boolean.toString(tooltipHint));
        p.setProperty("holdMs", Integer.toString(holdMs));
        p.setProperty("shiftCommand", shiftCommand);
        p.setProperty("ctrlCommand", ctrlCommand);
        p.setProperty("sellerPattern", sellerPattern);
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                p.store(writer, "Seller Lookup ({seller} becomes the seller's name)");
            }
        } catch (IOException e) {
            SellerLookupMod.LOGGER.warn("Could not write {}", path, e);
        }
    }
}
