package dev.totem.core.api.v1.manual;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/** Core-owned first-join chapter and lifecycle registration. */
public final class TotemManualOnboarding {
    public static final Identifier SECTION_ID =
            Identifier.fromNamespaceAndPath("totem", "getting_started");
    public static final TotemManualSection SECTION = new TotemManualSection(
            SECTION_ID,
            -1_000,
            "book.totem.manual.getting_started.title",
            List.of(
                    "book.totem.manual.getting_started.page.1",
                    "book.totem.manual.getting_started.page.2",
                    "book.totem.manual.getting_started.page.3"
            )
    );

    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private TotemManualOnboarding() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) {
            return;
        }
        TotemManualRegistry.global().register(SECTION);
        TotemManualLifecycle.registerLoginRefresh();
    }
}
