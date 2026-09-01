package dev.totem.core.api.v1.gamerule;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/** Shared vanilla game-rule categories contributed by TotemCore. */
public final class TotemGameRuleCategories {
    /** Groups every loaded Totem module's rules in the vanilla Game Rules screen. */
    public static final GameRuleCategory TOTEM = GameRuleCategory.register(
            Identifier.fromNamespaceAndPath("totem", "rules")
    );

    private TotemGameRuleCategories() {
    }

    /** Ensures the shared category is registered during TotemCore initialization. */
    public static void register() {
        // Static initialization owns the one shared category instance.
    }
}
