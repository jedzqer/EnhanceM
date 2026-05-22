package net.enhancem.mixin;

import net.enhancem.access.EnhancemEnderBreathMarker;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public class EnderBreathProjectileMixin implements EnhancemEnderBreathMarker {

	@Unique
	private static final double ENHANCEM_ENDER_BREATH_RADIUS = 1.75D;
	@Unique
	private static final double ENHANCEM_ENDER_BREATH_CHANCE = 0.5D;
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

	@Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/phys/Vec3;)V", at = @At("TAIL"))
	private void enhancem$markVanillaDragonBreath(Level level, LivingEntity owner, Vec3 direction, CallbackInfo ci) {
		if (!(owner instanceof EnderDragon dragon)) {
			return;
		}
		this.enhancem$customEnderBreath = dragon.getRandom().nextDouble() < ENHANCEM_ENDER_BREATH_CHANCE;
	}

	@ModifyArg(
			method = "onHit",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
			)
	)
	private Entity enhancem$replaceVanillaBreathCloud(Entity entity) {
		if (!this.enhancem$customEnderBreath || !(entity instanceof AreaEffectCloud cloud)) {
			return entity;
		}

		((EnhancemEnderBreathMarker) cloud).enhancem$setCustomEnderBreath(true);
		cloud.setRadius((float) ENHANCEM_ENDER_BREATH_RADIUS);
		return entity;
	}
}
