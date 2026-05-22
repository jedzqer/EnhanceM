package net.enhancem.mixin;

import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public class EnderDragonFireballRateMixin {

	@Unique
	private static final int ENHANCEM_FIREBALL_CHARGE_THRESHOLD = 3;
	@Unique
	private static final int ENHANCEM_EXTRA_FIREBALLS = 2;
	@Unique
	private int enhancem$extraFireballsRemaining;

	@Inject(method = "begin", at = @At("TAIL"))
	private void enhancem$resetExtraFireballs(CallbackInfo ci) {
		this.enhancem$extraFireballsRemaining = ENHANCEM_EXTRA_FIREBALLS;
	}

	@ModifyConstant(method = "doServerTick", constant = @Constant(intValue = 5))
	private int enhancem$lowerFireballChargeThreshold(int original) {
		return ENHANCEM_FIREBALL_CHARGE_THRESHOLD;
	}

	@Redirect(
		method = "doServerTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V",
			ordinal = 1
		)
	)
	private void enhancem$keepStrafingAfterFireball(EnderDragonPhaseManager phaseManager, EnderDragonPhase<?> phase) {
		if (phase == EnderDragonPhase.HOLDING_PATTERN && this.enhancem$extraFireballsRemaining > 0) {
			this.enhancem$extraFireballsRemaining--;
			return;
		}

		phaseManager.setPhase(phase);
	}
}
