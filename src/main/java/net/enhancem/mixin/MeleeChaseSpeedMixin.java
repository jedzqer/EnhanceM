package net.enhancem.mixin;

import net.enhancem.ChaseSpeedAccess;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeChaseSpeedMixin {
    @Shadow @Final protected PathfinderMob mob;
    @Shadow @Final private double speedModifier;

    // Vanilla pursuit owns path finding, movement and stopping (including creeper swelling).
    // Read the variant dynamically: goals are constructed before spawn/load assigns it.
    @Redirect(method = {"start", "tick"}, at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;speedModifier:D"), require = 2)
    private double enhancem$useChaseSpeed(MeleeAttackGoal goal) {
        return this.speedModifier * (this.mob instanceof ChaseSpeedAccess access
            ? access.enhancem$getChaseSpeedMultiplier() : 1.0D);
    }
}
