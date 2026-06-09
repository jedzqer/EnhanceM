package net.enhancem.mixin;

import net.enhancem.SwordZombieAccess;
import net.enhancem.SwordZombieAttackAnimation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class ZombieSwordMeleeAttackMixin {

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
	private int enhancem$swordAttackPhase = SwordZombieAttackAnimation.PHASE_NONE;
	@Unique
	private int enhancem$phaseTicksRemaining;
	@Unique
	private int enhancem$phaseTicksElapsed;
	@Unique
	private boolean enhancem$phaseDamageDealt;

	@Inject(method = "resetAttackCooldown", at = @At("HEAD"), cancellable = true)
	private void enhancem$applySwordZombieAttackCooldownPattern(CallbackInfo ci) {
		if (!this.enhancem$isSwordZombie()) {
			return;
		}

		this.ticksUntilNextAttack = 0;
		ci.cancel();
	}

	@Inject(method = "getAttackInterval", at = @At("HEAD"), cancellable = true)
	private void enhancem$returnSwordZombieAttackInterval(CallbackInfoReturnable<Integer> cir) {
		if (this.enhancem$isSwordZombie()) {
			cir.setReturnValue(SwordZombieAttackAnimation.TOTAL_COMBO_DURATION_TICKS);
		}
	}

	@Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
	private void enhancem$keepGoalAliveDuringSwordCombo(CallbackInfoReturnable<Boolean> cir) {
		if (this.enhancem$swordAttackPhase == SwordZombieAttackAnimation.PHASE_NONE) {
			return;
		}
		if (!(this.mob instanceof Zombie zombie) || !this.enhancem$isSwordZombie()) {
			return;
		}
		LivingEntity target = zombie.getTarget();
		if (target != null && target.isAlive()) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"), cancellable = true)
	private void enhancem$startSwordComboAtAttackWindowEnd(LivingEntity target, CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie) || zombie.level().isClientSide() || !this.enhancem$isSwordZombie()) {
			return;
		}

		if (this.enhancem$swordAttackPhase != SwordZombieAttackAnimation.PHASE_NONE) {
			this.enhancem$faceTarget(zombie, target);
			ci.cancel();
			return;
		}

		if (this.ticksUntilNextAttack > 0) {
			ci.cancel();
			return;
		}

		if (this.canPerformAttack(target)) {
			this.resetAttackCooldown();
			this.ticksUntilNextAttack += SwordZombieAttackAnimation.TOTAL_COMBO_DURATION_TICKS;
			this.enhancem$beginSwordAttackPhase(zombie, SwordZombieAttackAnimation.PHASE_FIRST);
			this.enhancem$faceTarget(zombie, target);
		}

		ci.cancel();
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$tickSwordCombo(CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie) || zombie.level().isClientSide() || !this.enhancem$isSwordZombie()) {
			return;
		}

		if (this.enhancem$swordAttackPhase == SwordZombieAttackAnimation.PHASE_NONE) {
			return;
		}

		LivingEntity target = zombie.getTarget();
		if (target == null || !target.isAlive()) {
			this.enhancem$stopSwordCombo(zombie);
			return;
		}

		this.enhancem$faceTarget(zombie, target);
		this.enhancem$phaseTicksElapsed++;

		if (!this.enhancem$phaseDamageDealt
			&& this.enhancem$phaseTicksElapsed >= SwordZombieAttackAnimation.getPhaseDamageTick(this.enhancem$swordAttackPhase)) {
			this.enhancem$phaseDamageDealt = true;
			if (zombie.isWithinMeleeAttackRange(target) && zombie.getSensing().hasLineOfSight(target)) {
				zombie.doHurtTarget((ServerLevel)zombie.level(), target);
			}
		}

		if (--this.enhancem$phaseTicksRemaining > 0) {
			return;
		}

		if (this.enhancem$swordAttackPhase == SwordZombieAttackAnimation.PHASE_FIRST) {
			this.enhancem$beginSwordAttackPhase(zombie, SwordZombieAttackAnimation.PHASE_SECOND);
			return;
		}

		this.enhancem$stopSwordCombo(zombie);
	}

	@Inject(method = "stop", at = @At("TAIL"))
	private void enhancem$clearSwordComboWhenGoalStops(CallbackInfo ci) {
		if (this.mob instanceof Zombie zombie && this.enhancem$isSwordZombie()) {
			this.enhancem$stopSwordCombo(zombie);
		}
	}

	@Unique
	private boolean enhancem$isSwordZombie() {
		return this.mob instanceof Zombie
			&& this.mob.getMainHandItem().is(ItemTags.SWORDS)
			&& !this.mob.getItemBySlot(EquipmentSlot.OFFHAND).is(Items.SHIELD);
	}

	@Unique
	private void enhancem$beginSwordAttackPhase(Zombie zombie, int phase) {
		this.enhancem$swordAttackPhase = phase;
		this.enhancem$phaseTicksRemaining = SwordZombieAttackAnimation.getPhaseDurationTicks(phase);
		this.enhancem$phaseTicksElapsed = 0;
		this.enhancem$phaseDamageDealt = false;
		((SwordZombieAccess)zombie).enhancem$setSwordAttackPhase(phase);
		zombie.swing(InteractionHand.MAIN_HAND);
	}

	@Unique
	private void enhancem$stopSwordCombo(Zombie zombie) {
		this.enhancem$swordAttackPhase = SwordZombieAttackAnimation.PHASE_NONE;
		this.enhancem$phaseTicksRemaining = 0;
		((SwordZombieAccess)zombie).enhancem$setSwordAttackPhase(SwordZombieAttackAnimation.PHASE_NONE);
	}

	@Unique
	private void enhancem$faceTarget(Zombie zombie, LivingEntity target) {
		float yaw = (float)(Mth.atan2(target.getZ() - zombie.getZ(), target.getX() - zombie.getX()) * 180.0F / (float)Math.PI) - 90.0F;
		zombie.setYRot(yaw);
		zombie.yBodyRot = yaw;
		zombie.yHeadRot = yaw;
		zombie.getLookControl().setLookAt(target, 360.0F, 360.0F);
	}
}
