package sellerlookup;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads who sells an item from the lines an auction house puts under it ("Seller: Notch", "Satıcı: [VIP] Ahmet",
 * "Owner » .BedrockName"...). Servers word this differently, so the line is matched loosely and the first word that
 * looks like a player name is taken (ranks such as "[VIP]" are skipped). config/sellerlookup.properties can set an
 * exact pattern instead.
 */
public final class Seller {
    // U: \w and \b know letters such as "ı", so "Satıcı:" ends a word
    private static final Pattern LABEL = Pattern.compile(
        "(?iuU)^\\W*(seller|satıcı|satici|owner|sahibi|sahip|listed by|sold by|by|player|oyuncu|vendeur|verkäufer|vendedor|venditore)\\b");
    /** A Java player name, or a Bedrock one with its "." or "*" in front (as Floodgate shows them). */
    private static final Pattern NAME = Pattern.compile("[.*]?[A-Za-z0-9_]{3,16}");
    private static final Pattern LEGACY_FORMAT = Pattern.compile("§.");

    private Seller() {
    }

    /**
     * @param lines    the item's lore, as plain text
     * @param pattern  a regular expression whose first group is the seller, or null for the loose matching
     * @return the seller's name, or null when no line names one
     */
    public static @Nullable String parse(List<String> lines, @Nullable String pattern) {
        for (int i = 0; i < lines.size(); i++) {
            String line = clean(lines.get(i));
            if (pattern != null) {
                String found = group(pattern, line);
                if (found != null) return found;
                continue;
            }
            Matcher m = LABEL.matcher(line);
            if (!m.find()) continue;
            String rest = line.substring(m.end()).replaceFirst("^[\\s:»>\\-–•|=]+", "").trim();
            if (rest.isEmpty() && i + 1 < lines.size()) rest = clean(lines.get(i + 1));
            String name = firstName(rest);
            if (name != null) return name;
        }
        return null;
    }

    /** "[VIP] Notch (online)" -> "Notch" */
    static @Nullable String firstName(String text) {
        for (String word : text.split("\\s+")) {
            String w = word.replaceAll("[,;!?)\\]]+$", "");
            if (NAME.matcher(w).matches()) return w;
        }
        return null;
    }

    /** The command to type: "/ah {seller}" -> "/ah Notch". */
    public static String command(String template, String seller) {
        return template.replace("{seller}", seller).trim();
    }

    private static String clean(String line) {
        return LEGACY_FORMAT.matcher(line).replaceAll("").trim();
    }

    private static @Nullable String group(String pattern, String line) {
        try {
            Matcher m = Pattern.compile(pattern).matcher(line);
            return m.find() && m.groupCount() >= 1 && !m.group(1).isBlank() ? m.group(1).trim() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }
}
