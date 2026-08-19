package dev.totem.core.migration;

import dev.totem.core.api.v1.migration.LegacyItemMigrationRegistry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Decode-safe placeholder for retained DeadRecall item identifiers. */
final class LegacyAliasItem extends Item {
    LegacyAliasItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack migrated = migrateHeld(player, hand);
        if (migrated.getItem() == this) {
            return super.use(level, player, hand);
        }
        return migrated.getItem().use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return super.useOn(context);
        }
        ItemStack migrated = migrateHeld(player, context.getHand());
        if (migrated.getItem() == this) {
            return super.useOn(context);
        }
        return migrated.getItem().useOn(context);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        ItemStack migrated = migrateHeld(player, hand);
        if (migrated.getItem() == this) {
            return super.interactLivingEntity(stack, player, target, hand);
        }
        return migrated.getItem().interactLivingEntity(migrated, player, target, hand);
    }

    private static ItemStack migrateHeld(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack migrated = LegacyItemMigrationRegistry.migrate(held);
        if (migrated != held) {
            player.setItemInHand(hand, migrated);
        }
        return migrated;
    }
}
