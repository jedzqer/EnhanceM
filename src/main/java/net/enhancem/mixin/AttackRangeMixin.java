package net.enhancem.mixin;

import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Mob.class)
public class AttackRangeMixin {

	@Unique
	private static final double HORIZONTAL_RANGE_MULTIPLIER = 2.0;
	@Unique
	private static final double VERTICAL_RANGE = 1.0;

	@ModifyArg(method = "getAttackBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"), index = 0)
	private double enhancem$increaseHorizontalRange(double x) {
		return x * HORIZONTAL_RANGE_MULTIPLIER;
	}

	@ModifyArg(method = "getAttackBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"), index = 1)
	private double enhancem$addVerticalRange(double y) {
		return VERTICAL_RANGE;
	}

	@ModifyArg(method = "getAttackBoundingBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/AABB;inflate(DDD)Lnet/minecraft/world/phys/AABB;"), index = 2)
	private double enhancem$increaseDepthRange(double z) {
		return z * HORIZONTAL_RANGE_MULTIPLIER;
	}
}
