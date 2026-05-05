package net.enhancem.mixin;

import net.enhancem.ShieldZombieAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
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

@Mixin(MeleeAttackGoal.class)
public abstract class ZombieShieldMeleeAttackMixin {

	@Unique
	private static final double SHIELD_BLOCK_START_DISTANCE_SQR = 9.0D;

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	protected abstract boolean canPerformAttack(LivingEntity target);

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
			&& !this.canPerformAttack(target);

		if (shouldBlock) {
			if (!zombie.isUsingItem() || zombie.getUsedItemHand() != InteractionHand.OFF_HAND) {
				zombie.startUsingItem(InteractionHand.OFF_HAND);
			}
			return;
		}

		this.enhancem$stopBlocking(zombie);
	}

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"))
	private void enhancem$lowerShieldBeforeAttack(LivingEntity target, CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie)) {
			return;
		}

		if (zombie.level().isClientSide() || !((ShieldZombieAccess) zombie).enhancem$isShieldZombie()) {
			return;
		}

		if (zombie.isUsingItem() && zombie.getUsedItemHand() == InteractionHand.OFF_HAND && this.canPerformAttack(target)) {
			zombie.stopUsingItem();
		}
	}

	@Inject(method = "stop", at = @At("TAIL"))
	private void enhancem$stopBlockingWhenGoalStops(CallbackInfo ci) {
		if (this.mob instanceof Zombie zombie) {
			this.enhancem$stopBlocking(zombie);
		}
	}

	@Unique
	private void enhancem$stopBlocking(Zombie zombie) {
		if (zombie.isUsingItem() && zombie.getUsedItemHand() == InteractionHand.OFF_HAND) {
			zombie.stopUsingItem();
		}
	}
}
