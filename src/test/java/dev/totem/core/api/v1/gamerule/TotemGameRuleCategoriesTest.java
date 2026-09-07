package dev.totem.core.api.v1.gamerule;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotemGameRuleCategoriesTest {
    private static final String CATEGORY_KEY = "gamerule.category.totem.rules";
    private static final Pattern FORMAT_PLACEHOLDER = Pattern.compile("%(?:\\d+\\$)?[a-zA-Z]");

    @Test
    void exposesOneStableVanillaCategory() {
        Identifier expected = Identifier.fromNamespaceAndPath("totem", "rules");

        assertEquals(expected, TotemGameRuleCategories.TOTEM.id());
        assertEquals(expected, TotemGameRuleCategories.TOTEM.getDescriptionId());
        assertEquals(CATEGORY_KEY, TotemGameRuleCategories.TOTEM.label().getString());
    }

    @Test
    void localeResourcesShipWithMatchingKeysAndFormatPlaceholders() {
        assertLocaleParity("totem-core");
        assertLocaleParity("totem");

        JsonObject traditionalChinese = language("totem-core", "zh_tw");
        JsonObject spanish = language("totem-core", "es_es");

        assertTrue(spanish.has(CATEGORY_KEY));
        assertTrue(traditionalChinese.has(CATEGORY_KEY));
        assertEquals("Totem 模組世界規則", traditionalChinese.get(CATEGORY_KEY).getAsString());
        assertEquals("Reglas mundiales de Totem", spanish.get(CATEGORY_KEY).getAsString());
    }

    private static void assertLocaleParity(String namespace) {
        JsonObject english = language(namespace, "en_us");
        for (String locale : List.of("zh_tw", "es_es")) {
            JsonObject localized = language(namespace, locale);
            assertEquals(english.keySet(), localized.keySet(), namespace + " " + locale + " keys");
            for (String key : english.keySet()) {
                String source = english.get(key).getAsString();
                String translated = localized.get(key).getAsString();
                assertFalse(translated.isBlank(), namespace + " " + locale + " " + key);
                assertEquals(
                        formatPlaceholders(source),
                        formatPlaceholders(translated),
                        namespace + " " + locale + " " + key
                );
            }
        }
    }

    private static List<String> formatPlaceholders(String value) {
        Matcher matcher = FORMAT_PLACEHOLDER.matcher(value);
        java.util.ArrayList<String> placeholders = new java.util.ArrayList<>();
        while (matcher.find()) {
            placeholders.add(matcher.group());
        }
        return placeholders;
    }

    private static JsonObject language(String namespace, String locale) {
        String path = "/assets/" + namespace + "/lang/" + locale + ".json";
        var stream = TotemGameRuleCategoriesTest.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing language resource: " + path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException exception) {
            throw new AssertionError("Could not read language resource: " + path, exception);
        }
    }
}
