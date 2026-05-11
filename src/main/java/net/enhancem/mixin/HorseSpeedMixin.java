package net.enhancem.mixin;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Horse.class)
public class HorseSpeedMixin {

	@Unique
	private static final double ENHANCEM_HORSE_SPEED_MULTIPLIER = 2.0D;

	@Inject(method = "randomizeAttributes", at = @At("TAIL"))
	private void enhancem$doubleHorseSpeed(RandomSource random, CallbackInfo ci) {
		Horse horse = (Horse) (Object) this;
		AttributeInstance speedAttr = horse.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speedAttr != null) {
			speedAttr.setBaseValue(speedAttr.getBaseValue() * ENHANCEM_HORSE_SPEED_MULTIPLIER);
		}
	}
}
