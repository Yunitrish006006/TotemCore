package dev.totem.core;

import dev.totem.core.api.v1.version.ApiVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiVersionTest {
    @Test
    void acceptsOnlyCompatibleMajorVersions() {
        assertTrue(ApiVersion.V1.supports(new ApiVersion(1, 0)));
        assertFalse(ApiVersion.V1.supports(new ApiVersion(2, 0)));
    }

    @Test
    void rejectsVersionsThatRequireANewerMinorContract() {
        assertFalse(ApiVersion.V1.supports(new ApiVersion(1, 1)));
    }
}
