package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class PiglinBruteHitEffectMixin {

	@Unique
	private static final int ENHANCEM_MELEE_SLOWNESS_DURATION_TICKS = 20;
	@Unique
	private static final int ENHANCEM_MELEE_SLOWNESS_AMPLIFIER = 0;
	@Unique
	private static final int ENHANCEM_FORCED_CROUCH_DURATION_TICKS = 20;
	@Unique
	private int enhancem$forcedCrouchTicks;

	@Inject(method = "hurtServer", at = @At("TAIL"))
	private void enhancem$applyHitEffects(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
		LivingEntity target = (LivingEntity) (Object) this;
		if (!cir.getReturnValue() || !(target instanceof Player player) || !player.isAlive()) {
			return;
		}

		if (!(source.getEntity() instanceof PiglinBrute brute) || source.getDirectEntity() != brute) {
			return;
		}

		player.addEffect(
			new MobEffectInstance(MobEffects.SLOWNESS, ENHANCEM_MELEE_SLOWNESS_DURATION_TICKS, ENHANCEM_MELEE_SLOWNESS_AMPLIFIER),
			brute
		);
		this.enhancem$forcedCrouchTicks = ENHANCEM_FORCED_CROUCH_DURATION_TICKS;
		player.setSwimming(true);
		player.setPose(Pose.SWIMMING);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$tickForcedCrouch(CallbackInfo ci) {
		LivingEntity target = (LivingEntity) (Object) this;
		if (this.enhancem$forcedCrouchTicks <= 0) {
			return;
		}

		if (!(target instanceof Player player) || !player.isAlive()) {
			this.enhancem$clearForcedCrouch(target);
			return;
		}

		player.setSwimming(true);
		player.setPose(Pose.SWIMMING);
		this.enhancem$forcedCrouchTicks--;
		if (this.enhancem$forcedCrouchTicks <= 0) {
			this.enhancem$clearForcedCrouch(target);
		}
	}

	@Unique
	private void enhancem$clearForcedCrouch(LivingEntity target) {
		if (target instanceof Player player && player.getPose() == Pose.SWIMMING) {
			player.setSwimming(false);
		}
		this.enhancem$forcedCrouchTicks = 0;
	}
}
