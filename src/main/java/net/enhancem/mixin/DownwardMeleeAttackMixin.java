package net.enhancem.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class DownwardMeleeAttackMixin {

	@Unique
	private static final double EXTRA_DOWNWARD_ATTACK_DEPTH = 0.5D;

	@Inject(method = "getAttackBoundingBox", at = @At("RETURN"), cancellable = true)
	private void enhancem$extendDownwardAttackBox(double horizontalExpansion, CallbackInfoReturnable<AABB> cir) {
		if ((Object)this instanceof Monster) {
			// Keep vanilla reach and mounted bounds; only lower the bottom face.
			cir.setReturnValue(cir.getReturnValue().expandTowards(0.0D, -EXTRA_DOWNWARD_ATTACK_DEPTH, 0.0D));
		}
	}
}
