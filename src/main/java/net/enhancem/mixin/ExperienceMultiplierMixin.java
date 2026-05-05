package net.enhancem.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperienceOrb.class)
public class ExperienceMultiplierMixin {

	@Unique
	private static final double EXPERIENCE_MULTIPLIER = 3.0;

	@Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
	private void onGetValue(CallbackInfoReturnable<Integer> cir) {
		cir.setReturnValue((int) (cir.getReturnValue() * EXPERIENCE_MULTIPLIER));
	}
}