package net.enhancem.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public class FleeWhenCloseMixin {

	@Shadow
	@Final
	private Monster mob;

	@Shadow
	private int attackTime;

	@Shadow
	private int seeTime;

	private static final double FLEE_DISTANCE_SQ = 36.0;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void stopAndFleeWhenTooClose(CallbackInfo ci) {
		LivingEntity target = this.mob.getTarget();
		if (target == null) return;

		double distSq = this.mob.distanceToSqr(target);

		if (distSq < FLEE_DISTANCE_SQ) {
			if (this.mob.isUsingItem()) {
				this.mob.stopUsingItem();
			}
			this.attackTime = 10;
			this.seeTime = -60;

			this.mob.getNavigation().stop();

			double fleeX = this.mob.getX() + (this.mob.getX() - target.getX());
			double fleeZ = this.mob.getZ() + (this.mob.getZ() - target.getZ());
			this.mob.getNavigation().moveTo(fleeX, this.mob.getY(), fleeZ, 1.5);

			this.mob.setAggressive(false);
			ci.cancel();
		}
	}
}