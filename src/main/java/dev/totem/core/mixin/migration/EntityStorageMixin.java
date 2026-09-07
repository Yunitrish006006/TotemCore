package dev.totem.core.mixin.migration;

import dev.totem.core.api.v1.migration.LegacyNbtMigrationRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Rewrites detached entity-region NBT before entities decode potion and effect components. */
@Mixin(EntityStorage.class)
abstract class EntityStorageMixin {
    @Inject(method = "lambda$loadEntities$0", at = @At("HEAD"))
    private void totemCore$migrateLegacyNbtBeforeEntityDecode(
            ChunkPos chunkPos,
            Optional<CompoundTag> tag,
            CallbackInfoReturnable<ChunkEntities<Entity>> callback
    ) {
        tag.ifPresent(LegacyNbtMigrationRegistry::migrate);
    }
}
