package net.enhancem.mixin;

import net.enhancem.ShieldZombieAccess;
import net.enhancem.SwordZombieAccess;
import net.enhancem.SwordZombieAttackAnimation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class ZombieShieldMeleeAttackMixin {

	@Unique
	private static final double SHIELD_BLOCK_START_DISTANCE_SQR = 9.0D;

	@Unique
	private boolean enhancem$guardedAttackPending;
	@Unique
	private int enhancem$guardedAttackTicks;
	@Unique
	private boolean enhancem$guardedAttackActive;

	@Shadow
	protected abstract void resetAttackCooldown();

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	protected abstract boolean canPerformAttack(LivingEntity target);

	@Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
	private void enhancem$finishGuardedAttack(CallbackInfoReturnable<Boolean> cir) {
		if ((this.enhancem$guardedAttackActive || this.enhancem$guardedAttackPending)
			&& this.mob.getMainHandItem().is(ItemTags.SWORDS)
			&& this.mob.getOffhandItem().is(net.minecraft.world.item.Items.SHIELD)) {
			LivingEntity target = this.mob.getTarget();
			if (target != null && target.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)
				&& this.mob.isWithinHome(target.blockPosition())) {
				cir.setReturnValue(true);
			}
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$blockBetweenAttacks(CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie)) {
			return;
		}

		if (zombie.level().isClientSide() || !((ShieldZombieAccess) zombie).enhancem$isShieldZombie()) {
			return;
		}

		LivingEntity target = zombie.getTarget();
		boolean shouldBlock = target != null
			&& target.isAlive()
			&& zombie.getOffhandItem().is(net.minecraft.world.item.Items.SHIELD)
			&& zombie.getSensing().hasLineOfSight(target)
			&& zombie.distanceToSqr(target) <= SHIELD_BLOCK_START_DISTANCE_SQR
			&& (!this.canPerformAttack(target) || this.enhancem$guardedAttackPending);

		if (shouldBlock) {
			if (!zombie.isUsingItem() || zombie.getUsedItemHand() != InteractionHand.OFF_HAND) {
				zombie.startUsingItem(InteractionHand.OFF_HAND);
			}
			return;
		}

		this.enhancem$guardedAttackPending = false;
		this.enhancem$stopBlocking(zombie);
	}

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"), cancellable = true)
	private void enhancem$chooseShieldAttackStance(LivingEntity target, CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie)) {
			return;
		}

		if (zombie.level().isClientSide() || !((ShieldZombieAccess) zombie).enhancem$isShieldZombie()) {
			return;
		}

		boolean swordAndShield = zombie.getMainHandItem().is(ItemTags.SWORDS)
			&& zombie.getOffhandItem().is(net.minecraft.world.item.Items.SHIELD);
		if (this.enhancem$guardedAttackActive) {
			if (!swordAndShield || !target.isAlive() || !zombie.isUsingItem()
				|| zombie.getUsedItemHand() != InteractionHand.OFF_HAND) {
				this.enhancem$clearGuardedAttack(zombie);
				ci.cancel();
				return;
			}
			this.enhancem$faceGuardedAttackTarget(zombie, target);
			this.enhancem$guardedAttackTicks++;
			if (this.enhancem$guardedAttackTicks == SwordZombieAttackAnimation.GUARDED_THRUST_DAMAGE_TICK
				&& zombie.isBlocking() && zombie.isWithinMeleeAttackRange(target)
				&& zombie.getSensing().hasLineOfSight(target)) {
				zombie.doHurtTarget((ServerLevel)zombie.level(), target);
			}
			if (this.enhancem$guardedAttackTicks >= SwordZombieAttackAnimation.GUARDED_THRUST_DURATION_TICKS) {
				this.enhancem$clearGuardedAttack(zombie);
			}
			ci.cancel();
			return;
		}
		if (!swordAndShield || !target.isAlive()
			|| !zombie.isWithinMeleeAttackRange(target) || !zombie.getSensing().hasLineOfSight(target)) {
			this.enhancem$guardedAttackPending = false;
		}
		if (!this.canPerformAttack(target)) {
			return;
		}

		// Choose once per attack, including any shield-raising windup.
		if (swordAndShield && (this.enhancem$guardedAttackPending || zombie.getRandom().nextBoolean())) {
			this.enhancem$faceGuardedAttackTarget(zombie, target);
			this.enhancem$guardedAttackPending = true;
			if (zombie.isUsingItem() && zombie.getUsedItemHand() != InteractionHand.OFF_HAND) {
				zombie.stopUsingItem();
			}
			if (!zombie.isUsingItem()) {
				zombie.startUsingItem(InteractionHand.OFF_HAND);
			}
			// Respect the shield's activation delay; the hit must land while blocking.
			if (!zombie.isBlocking()) {
				ci.cancel();
				return;
			}
			this.enhancem$guardedAttackPending = false;
			this.enhancem$guardedAttackActive = true;
			this.enhancem$guardedAttackTicks = 0;
			this.resetAttackCooldown();
			((SwordZombieAccess)zombie).enhancem$setSwordAttackPhase(SwordZombieAttackAnimation.PHASE_GUARDED_THRUST);
			zombie.swing(InteractionHand.MAIN_HAND);
			ci.cancel();
			return;
		}

		this.enhancem$stopBlocking(zombie);
	}

	@Inject(method = "stop", at = @At("TAIL"))
	private void enhancem$stopBlockingWhenGoalStops(CallbackInfo ci) {
		this.enhancem$guardedAttackPending = false;
		if (this.mob instanceof Zombie zombie) {
			this.enhancem$stopBlocking(zombie);
		}
	}

	@Unique
	private void enhancem$stopBlocking(Zombie zombie) {
		this.enhancem$clearGuardedAttack(zombie);
		if (zombie.isUsingItem() && zombie.getUsedItemHand() == InteractionHand.OFF_HAND) {
			zombie.stopUsingItem();
		}
	}

	@Unique
	private void enhancem$faceGuardedAttackTarget(Zombie zombie, LivingEntity target) {
		float yaw = (float)(Mth.atan2(target.getZ() - zombie.getZ(), target.getX() - zombie.getX()) * 180.0D / Math.PI) - 90.0F;
		zombie.setYRot(yaw);
		zombie.yBodyRot = yaw;
		zombie.yHeadRot = yaw;
		zombie.getLookControl().setLookAt(target, 360.0F, 360.0F);
	}

	@Unique
	private void enhancem$clearGuardedAttack(Zombie zombie) {
		if (this.enhancem$guardedAttackActive) {
			((SwordZombieAccess)zombie).enhancem$setSwordAttackPhase(SwordZombieAttackAnimation.PHASE_NONE);
		}
		this.enhancem$guardedAttackActive = false;
		this.enhancem$guardedAttackTicks = 0;
		this.enhancem$guardedAttackPending = false;
	}
}
