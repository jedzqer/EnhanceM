package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class ZombieAxeMeleeAttackMixin {

	@Unique
	private static final double AXE_ZOMBIE_RETREAT_DISTANCE = 1.4D;
	@Unique
	private static final double AXE_ZOMBIE_PREFERRED_DISTANCE_SQR = 9.0D;
	@Unique
	private static final double AXE_ZOMBIE_RETREAT_SPEED = 1.15D;
	@Unique
	private static final double AXE_ZOMBIE_JUMP_TRIGGER_DISTANCE_SQR = 4.0D;
	@Unique
	private static final double MIN_RETREAT_DIRECTION_LENGTH_SQR = 1.0E-6D;
	@Unique
	private static final int BACK_JUMP_CHANCE = 15;
	@Unique
	private static final int SIDE_JUMP_CHANCE = 15;
	@Unique
	private static final double AXE_ZOMBIE_BACK_JUMP_STRENGTH = 0.9D;
	@Unique
	private static final double AXE_ZOMBIE_SIDE_JUMP_STRENGTH = 0.55D;
	@Unique
	private static final double AXE_ZOMBIE_BACK_JUMP_VERTICAL = 0.42D;
	@Unique
	private static final double AXE_ZOMBIE_SIDE_JUMP_VERTICAL = 0.34D;
	@Unique
	private static final int POST_JUMP_COOLDOWN_TICKS = 4;

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	private int ticksUntilNextAttack;

	@Shadow
	protected abstract boolean canPerformAttack(LivingEntity target);

	@Shadow
	protected abstract void resetAttackCooldown();

	@Unique
	private int enhancem$jumpCooldownTicks;

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"), cancellable = true)
	private void enhancem$attackNearestPlayer(LivingEntity target, CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie) || zombie.level().isClientSide() || !this.enhancem$isAxeZombie()) {
			return;
		}

		LivingEntity attackTarget = this.enhancem$selectNearestPlayerTarget(zombie, target);
		if (attackTarget != null) {
			zombie.setTarget(attackTarget);
		}

		if (attackTarget != null && this.canPerformAttack(attackTarget)) {
			this.resetAttackCooldown();
			zombie.swing(InteractionHand.MAIN_HAND);
			zombie.doHurtTarget((ServerLevel)zombie.level(), attackTarget);
		}

		ci.cancel();
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$retreatDuringAxeCooldown(CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie) || zombie.level().isClientSide()) {
			return;
		}

		LivingEntity target = zombie.getTarget();
		if (!this.enhancem$shouldRetreatFromPlayer(target)) {
			this.enhancem$jumpCooldownTicks = 0;
			return;
		}

		if (this.enhancem$jumpCooldownTicks > 0) {
			this.enhancem$jumpCooldownTicks--;
			return;
		}

		this.enhancem$faceTarget(zombie, target);
		if (zombie.distanceToSqr(target) <= AXE_ZOMBIE_JUMP_TRIGGER_DISTANCE_SQR && this.enhancem$applyDodgeJump(zombie, target)) {
			return;
		}

		if (zombie.distanceToSqr(target) >= AXE_ZOMBIE_PREFERRED_DISTANCE_SQR) {
			zombie.getNavigation().stop();
			return;
		}

		Vec3 retreatDirection = this.enhancem$getRetreatDirection(target);
		double retreatX = zombie.getX() + retreatDirection.x * AXE_ZOMBIE_RETREAT_DISTANCE;
		double retreatZ = zombie.getZ() + retreatDirection.z * AXE_ZOMBIE_RETREAT_DISTANCE;
		zombie.getNavigation().moveTo(retreatX, zombie.getY(), retreatZ, AXE_ZOMBIE_RETREAT_SPEED);
	}

	@Unique
	private boolean enhancem$isAxeZombie() {
		return this.mob instanceof Zombie && this.mob.getMainHandItem().is(ItemTags.AXES);
	}

	@Unique
	private LivingEntity enhancem$selectNearestPlayerTarget(Zombie zombie, LivingEntity fallbackTarget) {
		Player nearestPlayer = zombie.level().getNearestPlayer(zombie, zombie.getAttributeValue(Attributes.FOLLOW_RANGE));
		if (nearestPlayer != null && nearestPlayer.isAlive()) {
			return nearestPlayer;
		}
		return fallbackTarget;
	}

	@Unique
	private boolean enhancem$shouldRetreatFromPlayer(LivingEntity target) {
		return this.enhancem$isAxeZombie()
			&& this.ticksUntilNextAttack > 0
			&& target instanceof Player
			&& target.isAlive();
	}

	@Unique
	private void enhancem$faceTarget(Zombie zombie, LivingEntity target) {
		float yaw = (float)(Mth.atan2(target.getZ() - zombie.getZ(), target.getX() - zombie.getX()) * 180.0F / (float)Math.PI) - 90.0F;
		zombie.setYRot(yaw);
		zombie.yBodyRot = yaw;
		zombie.yHeadRot = yaw;
		zombie.getLookControl().setLookAt(target, 360.0F, 360.0F);
	}

	@Unique
	private Vec3 enhancem$getRetreatDirection(LivingEntity target) {
		Vec3 retreatDirection = new Vec3(this.mob.getX() - target.getX(), 0.0D, this.mob.getZ() - target.getZ());
		if (retreatDirection.lengthSqr() >= MIN_RETREAT_DIRECTION_LENGTH_SQR) {
			return retreatDirection.normalize();
		}

		float yawRadians = this.mob.getYRot() * (float)(Math.PI / 180.0);
		return new Vec3(-Mth.sin(yawRadians), 0.0D, Mth.cos(yawRadians));
	}

	@Unique
	private boolean enhancem$applyDodgeJump(Zombie zombie, LivingEntity target) {
		Vec3 backward = this.enhancem$getRetreatDirection(target);
		Vec3 left = new Vec3(backward.z, 0.0D, -backward.x);
		Vec3 right = new Vec3(-backward.z, 0.0D, backward.x);
		int jumpType = this.enhancem$rollJumpType();
		if (jumpType < 0) {
			this.enhancem$jumpCooldownTicks = 0;
			return false;
		}

		Vec3 jumpDirection = switch (jumpType) {
			case 0 -> backward;
			case 1 -> left;
			default -> right;
		};
		boolean backwardJump = jumpType == 0;
		double horizontalStrength = backwardJump ? AXE_ZOMBIE_BACK_JUMP_STRENGTH : AXE_ZOMBIE_SIDE_JUMP_STRENGTH;
		double verticalStrength = backwardJump ? AXE_ZOMBIE_BACK_JUMP_VERTICAL : AXE_ZOMBIE_SIDE_JUMP_VERTICAL;
		Vec3 currentMovement = zombie.getDeltaMovement();
		zombie.setDeltaMovement(
			currentMovement.x + jumpDirection.x * horizontalStrength,
			Math.max(currentMovement.y, verticalStrength),
			currentMovement.z + jumpDirection.z * horizontalStrength
		);
		this.enhancem$jumpCooldownTicks = POST_JUMP_COOLDOWN_TICKS;
		return true;
	}

	@Unique
	private int enhancem$rollJumpType() {
		int roll = this.mob.getRandom().nextInt(100);
		if (roll < BACK_JUMP_CHANCE) {
			return 0;
		}
		if (roll < BACK_JUMP_CHANCE + SIDE_JUMP_CHANCE) {
			return 1;
		}
		if (roll < BACK_JUMP_CHANCE + SIDE_JUMP_CHANCE * 2) {
			return 2;
		}
		return -1;
	}
}
