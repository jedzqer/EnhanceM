package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragon.class)
public class EnderDragonBreathAttackMixin {

	@Unique
	private static final int ENHANCEM_BREATH_COOLDOWN_MIN = 120;
	@Unique
	private static final int ENHANCEM_BREATH_COOLDOWN_MAX = 220;
	@Unique
	private static final float ENHANCEM_BREATH_RADIUS = 4.0F;

	@Unique
	private int enhancem$breathCooldown;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void enhancem$initBreathCooldown(EntityType<?> entityType, Level level, CallbackInfo ci) {
		this.enhancem$breathCooldown = ENHANCEM_BREATH_COOLDOWN_MIN;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$spawnEnderBreath(CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		if (!(dragon.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (dragon.isDeadOrDying()) {
			return;
		}

		LivingEntity target = dragon.getTarget();
		if (!(target instanceof Player player) || !player.isAlive()) {
			return;
		}

		if (this.enhancem$breathCooldown > 0) {
			this.enhancem$breathCooldown--;
			return;
		}

		Vec3 forward = dragon.getViewVector(1.0F).normalize();
		Vec3 targetPos = player.position().add(0.0D, 0.25D, 0.0D);
		Vec3 spawnPos = targetPos.subtract(forward.scale(1.5D));

		AreaEffectCloud cloud = new AreaEffectCloud(serverLevel, spawnPos.x, spawnPos.y, spawnPos.z);
		cloud.setOwner(dragon);
		cloud.setRadius(ENHANCEM_BREATH_RADIUS);
		cloud.setDuration(100);
		cloud.setWaitTime(0);
		cloud.setRadiusPerTick(0.0F);
		serverLevel.addFreshEntity(cloud);

		this.enhancem$breathCooldown = ENHANCEM_BREATH_COOLDOWN_MIN + dragon.getRandom().nextInt(ENHANCEM_BREATH_COOLDOWN_MAX - ENHANCEM_BREATH_COOLDOWN_MIN + 1);
	}
}
