package net.enhancem;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class MobWeaponEnchantments {

    private MobWeaponEnchantments() {
    }

    public static void enhancePiglinLoadout(LivingEntity mob) {
        enchantPiglinWeapon(mob, mob.getItemBySlot(EquipmentSlot.MAINHAND));
        enchantPiglinWeapon(mob, mob.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    public static void enhanceZombifiedPiglinLoadout(LivingEntity mob) {
        enchantZombifiedPiglinWeapon(mob, mob.getItemBySlot(EquipmentSlot.MAINHAND));
        enchantZombifiedPiglinWeapon(mob, mob.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    private static void enchantPiglinWeapon(LivingEntity mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (stack.is(ItemTags.SPEARS)) {
            enchant(mob, stack, Enchantments.KNOCKBACK, 3);
            return;
        }

        if (stack.is(ItemTags.SWORDS)) {
            enchant(mob, stack, Enchantments.FIRE_ASPECT, 2);
        }
    }

    private static void enchantZombifiedPiglinWeapon(LivingEntity mob, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (stack.is(ItemTags.SPEARS)) {
            enchant(mob, stack, Enchantments.KNOCKBACK, 3);
            return;
        }

        if (stack.is(ItemTags.SWORDS)) {
            enchant(mob, stack, Enchantments.FIRE_ASPECT, 2);
        }
    }

    private static void enchant(LivingEntity mob, ItemStack stack, net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey, int level) {
        Holder.Reference<Enchantment> enchantment = mob.level()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT)
            .getOrThrow(enchantmentKey);
        stack.enchant(enchantment, level);
    }
}
