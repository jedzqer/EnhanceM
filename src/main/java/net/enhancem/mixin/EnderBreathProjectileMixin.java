package net.enhancem.mixin;

import net.enhancem.access.EnhancemEnderBreathMarker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(DragonFireball.class)
public class EnderBreathProjectileMixin implements EnhancemEnderBreathMarker {

	@Unique
	private static final double ENHANCEM_ENDER_BREATH_RADIUS = 1.75D;
	@Unique
	private static final double ENHANCEM_CLOUD_SEARCH_RADIUS = 4.0D;
	@Unique
	private boolean enhancem$customEnderBreath;

	@Override
	public boolean enhancem$isCustomEnderBreath() {
		return this.enhancem$customEnderBreath;
	}

	@Override
	public void enhancem$setCustomEnderBreath(boolean customEnderBreath) {
		this.enhancem$customEnderBreath = customEnderBreath;
	}

	@Inject(method = "onHit", at = @At("TAIL"))
	private void enhancem$markCustomBreathCloud(HitResult hitResult, CallbackInfo ci) {
		DragonFireball fireball = (DragonFireball) (Object) this;
		if (!this.enhancem$customEnderBreath) {
			return;
		}
		if (!(fireball.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		AABB bounds = new AABB(hitResult.getLocation(), hitResult.getLocation()).inflate(ENHANCEM_CLOUD_SEARCH_RADIUS);
		List<AreaEffectCloud> clouds = serverLevel.getEntitiesOfClass(
				AreaEffectCloud.class,
				bounds,
				cloud -> cloud.getOwner() == fireball.getOwner() && !((EnhancemEnderBreathMarker) cloud).enhancem$isCustomEnderBreath()
		);
		AreaEffectCloud cloud = clouds.stream()
				.min(Comparator.comparingDouble(candidate -> candidate.position().distanceToSqr(hitResult.getLocation())))
				.orElse(null);
		if (cloud == null) {
			return;
		}

		((EnhancemEnderBreathMarker) cloud).enhancem$setCustomEnderBreath(true);
		cloud.setRadius((float) ENHANCEM_ENDER_BREATH_RADIUS);
	}
}
