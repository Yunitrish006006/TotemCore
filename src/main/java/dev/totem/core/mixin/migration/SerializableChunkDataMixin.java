package dev.totem.core.mixin.migration;

import dev.totem.core.api.v1.migration.LegacyNbtMigrationRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Runs registered migrations before block-state and block-entity codecs read a chunk. */
@Mixin(SerializableChunkData.class)
abstract class SerializableChunkDataMixin {
    @Inject(method = "parse", at = @At("HEAD"))
    private static void totemCore$migrateLegacyNbtBeforeChunkDecode(
            LevelHeightAccessor levelHeightAccessor,
            PalettedContainerFactory containerFactory,
            CompoundTag tag,
            CallbackInfoReturnable<SerializableChunkData> callback
    ) {
        LegacyNbtMigrationRegistry.migrate(tag);
    }
}
