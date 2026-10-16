package net.enhancem.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.control.MoveControl;
import org.spongepowered.asm.mixin.injection.Redirect;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public class FleeWhenCloseMixin {

	@Shadow
	@Final
	private Monster mob;

	@Unique
	private static final double FLEE_ENTER_DIST_SQ = 36.0;
	@Unique
	private static final double FLEE_EXIT_DIST_SQ = 56.25;
	@Unique
	private static final double CHASE_ENTER_DIST_SQ = 225.0;
	@Unique
	private static final double CHASE_EXIT_DIST_SQ = 196.0;

	@Unique
	private boolean fleeing = false;
	@Unique
	private boolean chasing = false;

	@Unique private long enhancem$nextPathTick;
    @Unique private LivingEntity enhancem$movementTarget;

    @Inject(method = {"start", "stop"}, at = @At("TAIL"))
    private void enhancem$resetMovement(CallbackInfo ci) {
        this.fleeing = false;
        this.chasing = false;
        this.enhancem$movementTarget = null;
        this.enhancem$nextPathTick = 0;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void enhancem$updateMovementState(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            if (this.enhancem$movementTarget != null) this.mob.getNavigation().stop();
            this.enhancem$resetMovement(ci);
            return;
        }
        boolean wasFleeing = this.fleeing;
        boolean wasChasing = this.chasing;
        double distSq = this.mob.distanceToSqr(target);
        if (!this.fleeing && distSq < FLEE_ENTER_DIST_SQ) this.fleeing = true;
        else if (this.fleeing && distSq > FLEE_EXIT_DIST_SQ) this.fleeing = false;
        if (!this.chasing && distSq > CHASE_ENTER_DIST_SQ) this.chasing = true;
        else if (this.chasing && distSq < CHASE_EXIT_DIST_SQ) this.chasing = false;
        if (target != this.enhancem$movementTarget || wasFleeing != this.fleeing || wasChasing != this.chasing) {
            this.mob.getNavigation().stop();
            this.enhancem$nextPathTick = 0;
            this.enhancem$movementTarget = target;
        }
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;moveTo(Lnet/minecraft/world/entity/Entity;D)Z"))
    private boolean enhancem$throttlePursuit(PathNavigation navigation, Entity target, double speed) {
        if (this.fleeing || this.chasing) return true;
        long now = this.mob.level().getGameTime();
        if (now < this.enhancem$nextPathTick) return !navigation.isDone();
        boolean moved = navigation.moveTo(target, speed);
        this.enhancem$nextPathTick = now + (moved ? 6 + Math.floorMod(this.mob.getId(), 3) : 20);
        return moved;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/navigation/PathNavigation;stop()V"))
    private void enhancem$preserveCustomPath(PathNavigation navigation) {
        if (!this.fleeing && !this.chasing) navigation.stop();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/ai/control/MoveControl;strafe(FF)V"))
    private void enhancem$preserveCustomMovement(MoveControl control, float forward, float sideways) {
        if (!this.fleeing && !this.chasing) control.strafe(forward, sideways);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void fleeWhenTooClose(CallbackInfo ci) {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive() || !this.fleeing && !this.chasing) return;
        long now = this.mob.level().getGameTime();
        if (now < this.enhancem$nextPathTick) return;
        boolean moved;
        if (this.fleeing) {
            double fleeX = this.mob.getX() + (this.mob.getX() - target.getX());
            double fleeZ = this.mob.getZ() + (this.mob.getZ() - target.getZ());
            moved = this.mob.getNavigation().moveTo(fleeX, this.mob.getY(), fleeZ, 1.5);
        } else {
            moved = this.mob.getNavigation().moveTo(target, 1.5);
        }
        this.enhancem$nextPathTick = now + (moved ? 6 + Math.floorMod(this.mob.getId(), 3) : 20);
    }
}
