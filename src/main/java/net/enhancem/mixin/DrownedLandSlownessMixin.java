package net.enhancem.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public class DrownedLandSlownessMixin {

	@Unique
	private static final int ENHANCEM_DROWNED_LAND_SLOWNESS_DURATION = 10;
	@Unique
	private static final int ENHANCEM_DROWNED_LAND_SLOWNESS_AMPLIFIER = 3;

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$slowDrownedOnLand(CallbackInfo ci) {
		Mob mob = (Mob) (Object) this;
		if (!(mob instanceof Drowned drowned) || mob.level().isClientSide() || !mob.isAlive()) {
			return;
		}

		if (drowned.onGround() && !drowned.isInWater()) {
			drowned.addEffect(new MobEffectInstance(
				MobEffects.SLOWNESS,
				ENHANCEM_DROWNED_LAND_SLOWNESS_DURATION,
				ENHANCEM_DROWNED_LAND_SLOWNESS_AMPLIFIER,
				false,
				false,
				false
			));
		}
	}
}
