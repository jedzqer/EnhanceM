package net.enhancem.mixin;

import net.enhancem.ChaseSpeedAccess;
import net.enhancem.access.RetreatNavigationAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Shadow private int ticksUntilNextPathRecalculation;
    @Shadow private double pathedTargetX;
    @Shadow private double pathedTargetY;
    @Shadow private double pathedTargetZ;
    @Unique private boolean enhancem$wasRetreating;

    @Inject(method = "tick", at = @At("HEAD"))
    private void enhancem$updateNavigationOwner(CallbackInfo ci) {
        var target = this.mob.getTarget();
        boolean retreating = target != null && target.isAlive()
            && ((RetreatNavigationAccess) this).enhancem$isPiglinRetreating();
        if (!retreating && this.enhancem$wasRetreating) {
            this.mob.getNavigation().stop();
            this.ticksUntilNextPathRecalculation = 0;
            this.pathedTargetX = this.pathedTargetY = this.pathedTargetZ = 0;
        }
        this.enhancem$wasRetreating = retreating;
    }

    @Inject(method = {"start", "stop"}, at = @At("TAIL"))
    private void enhancem$resetNavigationOwner(CallbackInfo ci) {
        this.enhancem$wasRetreating = false;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;ID)Z"))
    private boolean enhancem$keepRetreatPath(PathNavigation navigation, Entity target, int reach, double speed) {
        return this.enhancem$wasRetreating || navigation.moveTo(target, reach, speed);
    }

    // Vanilla pursuit owns path finding, movement and stopping (including creeper swelling).
    // Read the variant dynamically: goals are constructed before spawn/load assigns it.
    @Redirect(method = {"start", "tick"}, at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;speedModifier:D"), require = 2)
    private double enhancem$useChaseSpeed(MeleeAttackGoal goal) {
        if (this.mob instanceof ZombifiedPiglin) return 1.1D;
        return this.speedModifier * (this.mob instanceof ChaseSpeedAccess access
            ? access.enhancem$getChaseSpeedMultiplier() : 1.0D);
    }
}
