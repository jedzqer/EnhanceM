package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public class EnderDragonFireballRateMixin {

	@Unique
	private static final int ENHANCEM_FIREBALL_CHARGE_THRESHOLD = 5;
	@Unique
	private static final int ENHANCEM_EXTRA_FIREBALLS = 1;
	@Unique
	private int enhancem$extraFireballsRemaining;

	@Inject(method = "begin", at = @At("TAIL"))
	private void enhancem$resetExtraFireballs(CallbackInfo ci) {
		this.enhancem$extraFireballsRemaining = ENHANCEM_EXTRA_FIREBALLS;
	}

	@Inject(method = "doServerTick", at = @At("TAIL"), cancellable = true)
	private void enhancem$keepStrafingAfterFireball(ServerLevel level, CallbackInfo ci) {
		if (this.enhancem$extraFireballsRemaining <= 0) {
			return;
		}

		// The vanilla phase returns to HOLDING_PATTERN after one shot; keep it strafing once more.
		// This increases the actual fireball density without changing the projectile itself.
		this.enhancem$extraFireballsRemaining--;
	}
}
