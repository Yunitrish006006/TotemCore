package dev.totem.core.api.v1.client.observer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ObserverScreenContractTest {
    @BeforeAll static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        RegistryAccess.Frozen builtInLookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        Set<?> builtInRegistryKeys = builtInLookup.listRegistryKeys().collect(Collectors.toSet());
        HolderLookup.Provider lookup = HolderLookup.Provider.create(Stream.concat(
                builtInLookup.listRegistries(),
                VanillaRegistries.createLookup().listRegistries()
                        .filter(registry -> !builtInRegistryKeys.contains(registry.key()))));
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(lookup).forEach(initializer -> initializer.apply());
        Bootstrap.validate();
    }

    @Test void snapshotDefensivelyCopiesBoundedState() {
        int[] data = {1, 2};
        byte[] payload = {3, 4};
        ItemStack sourceStack = new ItemStack(Items.DIAMOND, 2);
        var sourceTitle = Component.literal("Title");
        var snapshot = new ObserverScreenSnapshot("family", "variant", 1, 2,
                sourceTitle, List.of(sourceStack), data, Map.of("key", "value"), payload);
        data[0] = 9;
        payload[0] = 9;
        sourceStack.setCount(9);
        sourceTitle.append(" mutated");
        ItemStack exposedStack = snapshot.slots().getFirst();
        exposedStack.setCount(7);
        ((MutableComponent) snapshot.title()).append(" accessor mutation");
        assertArrayEquals(new int[]{1, 2}, snapshot.data());
        assertArrayEquals(new byte[]{3, 4}, snapshot.ownerPayload());
        assertEquals(2, snapshot.slots().getFirst().getCount());
        assertEquals("Title", snapshot.title().getString());
    }

    @Test void rejectsUnboundedOwnerPayload() {
        assertThrows(IllegalArgumentException.class, () -> new ObserverScreenSnapshot(
                "family", "", 1, 0, Component.empty(), List.of(), new int[0], Map.of(),
                new byte[ObserverScreenSnapshot.MAX_OWNER_PAYLOAD + 1]));
    }

    @Test void normalizesNullSlotEntriesWithoutExposingMutableState() {
        var snapshot = new ObserverScreenSnapshot("family", "variant", 1, 0,
                Component.empty(), java.util.Arrays.asList((ItemStack) null), new int[0], Map.of(), new byte[0]);
        assertTrue(snapshot.slots().getFirst().isEmpty());
    }

    @Test void rejectsUnboundedPlainTextTitleBeforeWireEncoding() {
        assertThrows(IllegalArgumentException.class, () -> new ObserverScreenSnapshot(
                "family", "", 1, 0, Component.literal("x".repeat(ObserverScreenSnapshot.MAX_TEXT + 1)),
                List.of(), new int[0], Map.of(), new byte[0]));
    }

    @Test void cursorMapsAcrossGuiScales() {
        ItemStack source = new ItemStack(Items.DIAMOND, 3);
        var cursor = new ObserverRemoteCursor(1, 88, 83, 176, 166, source);
        source.setCount(8);
        cursor.carriedStack().setCount(6);
        assertEquals(3, cursor.carriedStack().getCount());
        assertEquals(188.0, cursor.screenX(100, 176));
        assertEquals(103.0, cursor.screenY(20, 166));
    }

    @Test void rejectsUnboundedSnapshotCollectionsAndCursorGeometry() {
        assertThrows(IllegalArgumentException.class, () -> new ObserverScreenSnapshot(
                "family", "", 1, 0, Component.empty(),
                java.util.Collections.nCopies(ObserverScreenSnapshot.MAX_SLOTS + 1, ItemStack.EMPTY),
                new int[0], Map.of(), new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> new ObserverScreenSnapshot(
                "family", "", 1, 0, Component.empty(), List.of(),
                new int[ObserverScreenSnapshot.MAX_DATA + 1], Map.of(), new byte[0]));
        Map<String, String> metadata = new LinkedHashMap<>();
        for (int i = 0; i <= ObserverScreenSnapshot.MAX_METADATA; i++) metadata.put("key_" + i, "value");
        assertThrows(IllegalArgumentException.class, () -> new ObserverScreenSnapshot(
                "family", "", 1, 0, Component.empty(), List.of(), new int[0], metadata, new byte[0]));
        assertThrows(IllegalArgumentException.class,
                () -> new ObserverRemoteCursor(-1, 0, 0, 176, 166, ItemStack.EMPTY));
        assertThrows(IllegalArgumentException.class,
                () -> new ObserverRemoteCursor(1, 0, 0, 0, 166, ItemStack.EMPTY));
        assertThrows(IllegalArgumentException.class,
                () -> new ObserverRemoteCursor(1, Float.NaN, 0, 176, 166, ItemStack.EMPTY));
    }
}
