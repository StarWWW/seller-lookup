package sellerlookup;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SellerTest {
    private static String parse(String... lines) {
        return Seller.parse(List.of(lines), null);
    }

    @Test
    void plainLabel() {
        assertEquals("Notch", parse("Diamond Sword", "Seller: Notch", "Price: $1.5K"));
    }

    @Test
    void ranksAndColourCodesAreSkipped() {
        assertEquals("Notch", parse("Seller: [VIP] Notch"));
        assertEquals("Ahmet_123", parse("§7Satıcı: §f[MVP+] Ahmet_123"));
        assertEquals("Steve", parse("Sold by: Steve (online)"));
    }

    @Test
    void otherSeparatorsBedrockNamesAndTheNextLine() {
        assertEquals(".BedrockGuy", parse("Owner » .BedrockGuy"));
        assertEquals("Alex", parse("Seller:", "Alex"));
    }

    @Test
    void noSeller() {
        assertNull(parse("Price: $1.5K", "Expires: 1d 2h"));
        // a label without anything that looks like a name: nothing is guessed
        assertNull(parse("Seller: ???"));
    }

    @Test
    void ownPattern() {
        assertEquals("Notch", Seller.parse(List.of("[AH] Notch sells this"), "\\[AH\\] (\\S+) sells"));
        // a broken pattern finds nothing instead of crashing
        assertNull(Seller.parse(List.of("Seller: Notch"), "("));
    }

    @Test
    void command() {
        assertEquals("/ah Notch", Seller.command("/ah {seller}", "Notch"));
        assertEquals("/stats Notch", Seller.command(" /stats {seller} ", "Notch"));
    }
}
