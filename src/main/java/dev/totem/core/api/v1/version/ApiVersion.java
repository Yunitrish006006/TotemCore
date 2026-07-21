package dev.totem.core.api.v1.version;

/** The compatibility version for TotemCore's public API surface. */
public record ApiVersion(int major, int minor) {
    public static final ApiVersion V1 = new ApiVersion(1, 0);

    public ApiVersion {
        if (major < 1 || minor < 0) throw new IllegalArgumentException("Invalid API version");
    }

    public boolean supports(ApiVersion required) {
        return major == required.major && minor >= required.minor;
    }
}
