package net.enhancem.mixin;

import net.minecraft.world.entity.monster.zombie.Drowned;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Drowned.class)
public class DrownedSpeedMixin {

	@Unique
	private static final float ENHANCEM_DROWNED_SWIM_SPEED = 0.065F;

	@ModifyArg(
		method = "travelInWater",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/monster/zombie/Drowned;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"
		),
		index = 0
	)
	private float enhancem$boostSwimSpeed(float originalSpeed) {
		return ENHANCEM_DROWNED_SWIM_SPEED;
	}
}
