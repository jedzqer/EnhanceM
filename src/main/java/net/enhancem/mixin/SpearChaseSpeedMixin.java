package net.enhancem.mixin;

import net.enhancem.ChaseSpeedAccess;
import net.minecraft.world.entity.ai.goal.SpearUseGoal;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SpearUseGoal.class)
public abstract class SpearChaseSpeedMixin {
    @Shadow @Final private Monster mob;
    @Shadow @Final private double speedModifierWhenCharging;

    @Redirect(method = "tick", at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/entity/ai/goal/SpearUseGoal;speedModifierWhenCharging:D"))
    private double enhancem$useChaseSpeed(SpearUseGoal<?> goal) {
        return this.speedModifierWhenCharging * (this.mob instanceof ChaseSpeedAccess access
            ? access.enhancem$getChaseSpeedMultiplier() : 1.0D);
    }
}
