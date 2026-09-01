package dev.totem.core.api.v1.gamerule;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotemGameRuleCategoriesTest {
    private static final String CATEGORY_KEY = "gamerule.category.totem.rules";

    @Test
    void exposesOneStableVanillaCategory() {
        Identifier expected = Identifier.fromNamespaceAndPath("totem", "rules");

        assertEquals(expected, TotemGameRuleCategories.TOTEM.id());
        assertEquals(expected, TotemGameRuleCategories.TOTEM.getDescriptionId());
        assertEquals(CATEGORY_KEY, TotemGameRuleCategories.TOTEM.label().getString());
    }

    @Test
    void categoryLabelShipsInEnglishAndTraditionalChinese() {
        JsonObject english = language("en_us");
        JsonObject traditionalChinese = language("zh_tw");

        assertEquals(english.keySet(), traditionalChinese.keySet());
        assertTrue(english.has(CATEGORY_KEY));
        assertTrue(traditionalChinese.has(CATEGORY_KEY));
        assertFalse(english.get(CATEGORY_KEY).getAsString().isBlank());
        assertEquals("Totem 模組世界規則", traditionalChinese.get(CATEGORY_KEY).getAsString());
    }

    private static JsonObject language(String locale) {
        String path = "/assets/totem-core/lang/" + locale + ".json";
        var stream = TotemGameRuleCategoriesTest.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing language resource: " + path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException exception) {
            throw new AssertionError("Could not read language resource: " + path, exception);
        }
    }
}
