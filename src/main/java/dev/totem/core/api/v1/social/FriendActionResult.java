package dev.totem.core.api.v1.social;

/** Stable result contract for Totem-wide friendship invitation mutations. */
public enum FriendActionResult {
    INVITED,
    ACCEPTED,
    PENDING,
    ALREADY_FRIENDS,
    INVALID
}
