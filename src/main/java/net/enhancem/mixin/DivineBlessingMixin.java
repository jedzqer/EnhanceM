package net.enhancem.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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

        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.isEmpty()) return;

        var enchantmentRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var enchantmentHolder = enchantmentRegistry.get(DIVINE_BLESSING_KEY);
        if (enchantmentHolder.isEmpty()) return;

        Holder<Enchantment> holder = enchantmentHolder.get();
        if (EnchantmentHelper.getItemEnchantmentLevel(holder, chestplate) <= 0) return;

        enhancem$activateDivineBlessing(player);
        cir.setReturnValue(true);
    }

    @Unique
    private static void enhancem$activateDivineBlessing(Player player) {
        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
    }
}
