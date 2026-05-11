package net.enhancem.mixin;

import net.enhancem.MobWeaponEnchantments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Piglin.class)
public class PiglinWeaponEnchantMixin {

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void enhancem$enchantPiglinWeapons(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        Piglin piglin = (Piglin) (Object) this;
        MobWeaponEnchantments.enhancePiglinLoadout(piglin);
    }
}
