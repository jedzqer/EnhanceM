package net.enhancem.mixin;

import net.enhancem.EnhanceM;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DivineBlessingMixin {

    @Unique
    private static final ResourceKey<Enchantment> DIVINE_BLESSING_KEY = ResourceKey.create(
            Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath("enhancem", "divine_blessing")
    );

    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
    private void enhancem$checkDivineBlessingProtection(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (!((Object) this instanceof Player player)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        var enchantmentRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var enchantmentHolder = enchantmentRegistry.get(DIVINE_BLESSING_KEY);
        if (enchantmentHolder.isEmpty()) return;

        Holder<Enchantment> holder = enchantmentHolder.get();

        while (true) {
            EquipmentSlot blessedSlot = enhancem$findBlessedArmorSlot(player, holder);
            if (blessedSlot == null) return;

            if (enhancem$activateDivineBlessing(player, serverLevel, blessedSlot)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Unique
    private static EquipmentSlot enhancem$findBlessedArmorSlot(Player player, Holder<Enchantment> holder) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0) {
                return slot;
            }
        }
        return null;
    }

    @Unique
    private static boolean enhancem$activateDivineBlessing(Player player, ServerLevel serverLevel, EquipmentSlot blessedSlot) {
        ItemStack blessedStack = player.getItemBySlot(blessedSlot);
        if (!blessedStack.isEmpty() && blessedStack.isDamageableItem()) {
            int maxDamage = blessedStack.getMaxDamage();
            int remainingDurability = maxDamage - blessedStack.getDamageValue();
            int reservedDurability = Math.max(1, (int) Math.ceil(maxDamage * 0.05D));

            if (remainingDurability <= reservedDurability) {
                player.onEquippedItemBroken(blessedStack.getItem(), blessedSlot);
                player.setItemSlot(blessedSlot, ItemStack.EMPTY);
                return false;
            }

            ItemStack protectedStack = blessedStack.copy();
            protectedStack.setDamageValue(maxDamage - reservedDurability);
            player.setItemSlot(blessedSlot, protectedStack);
        }

        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));

        double x = player.getX(), y = player.getY(), z = player.getZ();
        serverLevel.playSound(null, x, y, z, EnhanceM.DIVINE_BLESSING_SOUND, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }
}
