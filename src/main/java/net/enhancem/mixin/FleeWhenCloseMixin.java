package net.enhancem.mixin;

import net.minecraft.world.entity.LivingEntity;
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
	private boolean fleeing = false;

	@Inject(method = "tick", at = @At("TAIL"))
	private void fleeWhenTooClose(CallbackInfo ci) {
		LivingEntity target = this.mob.getTarget();
		if (target == null) return;

		double distSq = this.mob.distanceToSqr(target);

		if (!this.fleeing && distSq < FLEE_ENTER_DIST_SQ) {
			this.fleeing = true;
		} else if (this.fleeing && distSq > FLEE_EXIT_DIST_SQ) {
			this.fleeing = false;
		}

		if (this.fleeing) {
			double fleeX = this.mob.getX() + (this.mob.getX() - target.getX());
			double fleeZ = this.mob.getZ() + (this.mob.getZ() - target.getZ());
			this.mob.getNavigation().moveTo(fleeX, this.mob.getY(), fleeZ, 1.5);
		}
	}
}
