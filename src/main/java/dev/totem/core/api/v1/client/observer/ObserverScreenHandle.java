package dev.totem.core.api.v1.client.observer;

import net.minecraft.client.gui.screens.Screen;

/**
 * Live owner-screen instance controlled by semantic snapshots. All methods are
 * invoked only on the Minecraft client thread.
 */
public interface ObserverScreenHandle {
    Screen screen();
    void applySnapshot(ObserverScreenSnapshot snapshot);
    void applyCursor(ObserverRemoteCursor cursor);
}
