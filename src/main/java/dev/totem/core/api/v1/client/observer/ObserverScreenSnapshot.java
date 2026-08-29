package dev.totem.core.api.v1.client.observer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Versioned, bounded transport envelope. The owner payload is opaque to the
 * coordinator and decoded only by the module that owns the production Screen.
 */
public record ObserverScreenSnapshot(
        String familyId,
        String variant,
        int protocolVersion,
        long sequence,
        Component title,
        List<ItemStack> slots,
        int[] data,
        Map<String, String> metadata,
        byte[] ownerPayload
) {
    public static final int MAX_SLOTS = 256;
    public static final int MAX_DATA = 128;
    public static final int MAX_METADATA = 64;
    public static final int MAX_TEXT = 2048;
    public static final int MAX_OWNER_PAYLOAD = 64 * 1024;

    public ObserverScreenSnapshot {
        familyId = bounded(familyId, 64, "familyId", false);
        variant = bounded(variant, 64, "variant", true);
        if (protocolVersion < 1 || sequence < 0) {
            throw new IllegalArgumentException("Invalid observer protocol version or sequence");
        }
        title = title == null ? Component.empty() : title.copy();
        if (title.getString().length() > MAX_TEXT)
            throw new IllegalArgumentException("Observer title is too long");
        List<ItemStack> sourceSlots = slots == null ? List.of() : slots;
        if (sourceSlots.size() > MAX_SLOTS) throw new IllegalArgumentException("Too many observer slots");
        slots = sourceSlots.stream().map(stack -> stack == null ? ItemStack.EMPTY : stack.copy()).toList();
        data = data == null ? new int[0] : data.clone();
        if (data.length > MAX_DATA) throw new IllegalArgumentException("Too much observer menu data");
        metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        if (metadata.size() > MAX_METADATA) throw new IllegalArgumentException("Too much observer metadata");
        metadata.forEach((key, value) -> {
            bounded(key, 128, "metadata key", false);
            bounded(value, MAX_TEXT, "metadata value", true);
        });
        ownerPayload = ownerPayload == null ? new byte[0] : ownerPayload.clone();
        if (ownerPayload.length > MAX_OWNER_PAYLOAD) throw new IllegalArgumentException("Observer owner payload too large");
    }

    private static String bounded(String value, int max, String field, boolean emptyAllowed) {
        if (value == null || (!emptyAllowed && value.isBlank()) || value.length() > max) {
            throw new IllegalArgumentException("Invalid " + field);
        }
        return value;
    }

    @Override public int[] data() { return data.clone(); }
    @Override public Component title() { return title.copy(); }
    @Override public List<ItemStack> slots() {
        return slots.stream().map(ItemStack::copy).toList();
    }
    @Override public byte[] ownerPayload() { return ownerPayload.clone(); }
}
