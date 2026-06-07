package net.enhancem.mixin;

import net.minecraft.tags.ItemTags;
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
public abstract class ZombieSwordMeleeAttackMixin {

	@Unique
	private static final int FAST_ATTACK_INTERVAL = 5;
	@Unique
	private static final int NORMAL_ATTACK_INTERVAL = 10;
	@Unique
	private static final int SLOW_ATTACK_INTERVAL = 15;
	@Unique
	private static final int FAST_ATTACK_CHANCE = 25;
	@Unique
	private static final int SLOW_ATTACK_CHANCE = 25;

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	private int ticksUntilNextAttack;

	@Unique
	private int enhancem$currentSwordAttackInterval = NORMAL_ATTACK_INTERVAL;

	@Inject(method = "resetAttackCooldown", at = @At("HEAD"), cancellable = true)
	private void enhancem$applySwordZombieAttackCooldownPattern(CallbackInfo ci) {
		if (!this.enhancem$isSwordZombie()) {
			return;
		}

		this.enhancem$currentSwordAttackInterval = this.enhancem$rollSwordAttackInterval();
		this.ticksUntilNextAttack = this.enhancem$currentSwordAttackInterval;
		ci.cancel();
	}

	@Inject(method = "getAttackInterval", at = @At("HEAD"), cancellable = true)
	private void enhancem$returnSwordZombieAttackInterval(CallbackInfoReturnable<Integer> cir) {
		if (this.enhancem$isSwordZombie()) {
			cir.setReturnValue(this.enhancem$currentSwordAttackInterval);
		}
	}

	@Unique
	private boolean enhancem$isSwordZombie() {
		return this.mob instanceof Zombie && this.mob.getMainHandItem().is(ItemTags.SWORDS);
	}

	@Unique
	private int enhancem$rollSwordAttackInterval() {
		int roll = this.mob.getRandom().nextInt(100);
		if (roll < FAST_ATTACK_CHANCE) {
			return FAST_ATTACK_INTERVAL;
		}
		if (roll < FAST_ATTACK_CHANCE + SLOW_ATTACK_CHANCE) {
			return SLOW_ATTACK_INTERVAL;
		}
		return NORMAL_ATTACK_INTERVAL;
	}
}
