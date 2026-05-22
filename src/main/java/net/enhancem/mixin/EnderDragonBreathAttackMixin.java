package net.enhancem.mixin;

import net.enhancem.access.EnhancemEnderBreathMarker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
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
	private static final double ENHANCEM_BREATH_TARGET_HEIGHT_OFFSET = 0.5D;

	@Unique
	private int enhancem$breathCooldown;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void enhancem$initBreathCooldown(EntityType<?> entityType, Level level, CallbackInfo ci) {
		this.enhancem$breathCooldown = ENHANCEM_BREATH_COOLDOWN_MIN;
	}

	@Inject(method = "aiStep", at = @At("TAIL"))
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

		Vec3 mouthPos = new Vec3(dragon.head.getX(), dragon.head.getY(0.5D), dragon.head.getZ());
		Vec3 targetPos = player.position().add(0.0D, ENHANCEM_BREATH_TARGET_HEIGHT_OFFSET, 0.0D);
		Vec3 direction = targetPos.subtract(mouthPos);
		if (direction.lengthSqr() < 1.0E-6D) {
			return;
		}

		DragonFireball fireball = new DragonFireball(serverLevel, dragon, direction);
		((EnhancemEnderBreathMarker) fireball).enhancem$setCustomEnderBreath(true);
		fireball.setPos(mouthPos.x, mouthPos.y, mouthPos.z);
		serverLevel.addFreshEntity(fireball);

		this.enhancem$breathCooldown = ENHANCEM_BREATH_COOLDOWN_MIN + dragon.getRandom().nextInt(ENHANCEM_BREATH_COOLDOWN_MAX - ENHANCEM_BREATH_COOLDOWN_MIN + 1);
	}
}
