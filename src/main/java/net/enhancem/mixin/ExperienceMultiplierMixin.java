package net.enhancem.mixin;

import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ExperienceOrb.class)
public class ExperienceMultiplierMixin {

	@Unique
	private static final int EXPERIENCE_MULTIPLIER = 3;

	@ModifyVariable(method = "awardWithDirection", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private static int enhancem$multiplyAwardedExperience(int amount) {
		if (amount <= 0 || ExperienceDropContext.isPlayerDeathXp()) {
			return amount;
		}

		return amount * EXPERIENCE_MULTIPLIER;
	}
}
