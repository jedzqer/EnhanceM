package net.enhancem.mixin;

import net.enhancem.MobWeaponEnchantments;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZombifiedPiglin.class)
public class ZombifiedPiglinWeaponEnchantMixin {

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("TAIL"))
    private void enhancem$enchantZombifiedPiglinWeapons(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        ZombifiedPiglin piglin = (ZombifiedPiglin) (Object) this;
        MobWeaponEnchantments.enhanceZombifiedPiglinLoadout(piglin);
    }
}
