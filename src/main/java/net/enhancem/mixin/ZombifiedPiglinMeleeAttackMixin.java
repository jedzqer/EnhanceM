package net.enhancem.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class ZombifiedPiglinMeleeAttackMixin {

	@Unique
	private static final int RETREAT_DURATION_TICKS = 12;
	@Unique
	private static final double APPROACH_SPEED = 1.1D;
	@Unique
	private static final double RETREAT_SPEED = 1.45D;
	@Unique
	private static final double RETREAT_DISTANCE = 3.0D;
	@Unique
	private static final double MIN_RETREAT_DIRECTION_LENGTH_SQR = 1.0E-6D;

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	protected abstract boolean isTimeToAttack();

	@Unique
	private int enhancem$retreatTicks;

	@Inject(method = "start", at = @At("TAIL"))
	private void enhancem$resetRetreatState(CallbackInfo ci) {
		this.enhancem$retreatTicks = 0;
	}

	@Inject(method = "stop", at = @At("TAIL"))
	private void enhancem$clearRetreatState(CallbackInfo ci) {
		this.enhancem$retreatTicks = 0;
	}

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"))
	private void enhancem$retreatAfterAttack(LivingEntity target, CallbackInfo ci) {
		if (!this.enhancem$isValidZombifiedPiglinTarget(target)) {
			return;
		}

		if (this.mob.isWithinMeleeAttackRange(target) && this.isTimeToAttack()) {
			this.enhancem$retreatTicks = RETREAT_DURATION_TICKS;
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$keepAttackSpacing(CallbackInfo ci) {
		if (!(this.mob instanceof ZombifiedPiglin piglin) || piglin.level().isClientSide()) {
			return;
		}

		LivingEntity target = piglin.getTarget();
		if (target == null || !target.isAlive()) {
			this.enhancem$retreatTicks = 0;
			return;
		}

		piglin.getLookControl().setLookAt(target, 30.0F, 30.0F);

		if (this.enhancem$retreatTicks > 0) {
			this.enhancem$retreatTicks--;
			this.enhancem$moveAwayFromTarget(target);
			return;
		}

		if (piglin.isWithinMeleeAttackRange(target)) {
			piglin.getNavigation().stop();
			return;
		}

		piglin.getNavigation().moveTo(target, APPROACH_SPEED);
	}

	@Unique
	private boolean enhancem$isValidZombifiedPiglinTarget(LivingEntity target) {
		return this.mob instanceof ZombifiedPiglin piglin
			&& !piglin.level().isClientSide()
			&& target != null
			&& target.isAlive();
	}

	@Unique
	private void enhancem$moveAwayFromTarget(LivingEntity target) {
		Vec3 retreatDirection = this.mob.position().subtract(target.position());
		if (retreatDirection.lengthSqr() < MIN_RETREAT_DIRECTION_LENGTH_SQR) {
			this.mob.getNavigation().stop();
			return;
		}

		Vec3 normalizedDirection = retreatDirection.normalize().scale(RETREAT_DISTANCE);
		double retreatX = this.mob.getX() + normalizedDirection.x;
		double retreatZ = this.mob.getZ() + normalizedDirection.z;
		this.mob.getNavigation().moveTo(retreatX, this.mob.getY(), retreatZ, RETREAT_SPEED);
	}
}
