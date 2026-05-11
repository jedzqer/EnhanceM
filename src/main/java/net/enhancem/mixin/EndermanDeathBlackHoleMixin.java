package net.enhancem.mixin;

import net.enhancem.EndermanBlackHoleManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class EndermanDeathBlackHoleMixin {

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void enhancem$spawnBlackHole(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof EnderMan enderman) || level.isClientSide()) {
            return;
        }

        EndermanBlackHoleManager.spawn(level, enderman);
    }
}
