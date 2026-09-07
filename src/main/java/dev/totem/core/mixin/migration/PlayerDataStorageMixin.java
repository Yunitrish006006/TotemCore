package dev.totem.core.mixin.migration;

import dev.totem.core.api.v1.migration.LegacyNbtMigrationRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Rewrites player data after vanilla data fixing and before Player.load decodes registry components. */
@Mixin(PlayerDataStorage.class)
abstract class PlayerDataStorageMixin {
    @Inject(method = "lambda$load$1", at = @At("RETURN"))
    private void totemCore$migrateLegacyNbtBeforePlayerDecode(
            CompoundTag tag,
            CallbackInfoReturnable<CompoundTag> callback
    ) {
        LegacyNbtMigrationRegistry.migrate(callback.getReturnValue());
    }
}
