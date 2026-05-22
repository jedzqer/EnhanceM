package net.enhancem.mixin;

import net.enhancem.EnhanceM;
import net.enhancem.entity.EnderSoldierEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnderDragon.class)
public class EnderDragonHealthMixin {

	@Unique
	private static final double ENHANCEM_HEALTH_MULTIPLIER = 2.0D;
	@Unique
	private static final int ENHANCEM_SUMMON_THRESHOLDS = 5;
	@Unique
	private static final int ENHANCEM_SUMMON_COUNT = 3;
	@Unique
	private static final double ENHANCEM_SUMMON_RADIUS = 18.0D;

	@Unique
	private int enhancem$nextSummonThresholdIndex = 1;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void enhancem$doubleEnderDragonHealth(EntityType<?> entityType, Level level, CallbackInfo ci) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		AttributeInstance healthAttr = dragon.getAttribute(Attributes.MAX_HEALTH);
		if (healthAttr != null) {
			healthAttr.setBaseValue(healthAttr.getBaseValue() * ENHANCEM_HEALTH_MULTIPLIER);
			dragon.setHealth(dragon.getMaxHealth());
		}
	}

	@Inject(method = "hurtServer", at = @At("TAIL"))
	private void enhancem$summonEnderSoldiers(ServerLevel serverLevel, net.minecraft.world.damagesource.DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
		EnderDragon dragon = (EnderDragon) (Object) this;
		if (!cir.getReturnValueZ() || dragon.isDeadOrDying()) {
			return;
		}

		while (this.enhancem$nextSummonThresholdIndex <= ENHANCEM_SUMMON_THRESHOLDS) {
			double threshold = dragon.getMaxHealth() * (1.0D - this.enhancem$nextSummonThresholdIndex * 0.2D);
			if (dragon.getHealth() > threshold) {
				break;
			}

			this.enhancem$spawnEnderSoldiers(serverLevel, dragon);
			this.enhancem$nextSummonThresholdIndex++;
		}
	}

	@Unique
	private void enhancem$spawnEnderSoldiers(ServerLevel serverLevel, EnderDragon dragon) {
		Player initialTarget = this.enhancem$findNearestPlayer(serverLevel, dragon.position());
		for (int i = 0; i < ENHANCEM_SUMMON_COUNT; i++) {
			BlockPos spawnPos = this.enhancem$findSpawnPos(serverLevel, dragon, i);
			if (spawnPos == null) {
				continue;
			}

			EnderSoldierEntity soldier = EnhanceM.ENDER_SOLDIER.create(serverLevel, net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
			if (soldier == null) {
				continue;
			}

			soldier.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
			soldier.setYRot(dragon.getRandom().nextFloat() * 360.0F);
			soldier.setXRot(0.0F);
			soldier.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED, null);
			soldier.setPersistenceRequired();
			if (initialTarget != null) {
				soldier.setTarget(initialTarget);
			}
			serverLevel.addFreshEntity(soldier);
		}
	}

	@Unique
	private BlockPos enhancem$findSpawnPos(ServerLevel serverLevel, EnderDragon dragon, int index) {
		double angle = (Math.PI * 2.0D / ENHANCEM_SUMMON_COUNT) * index + dragon.getRandom().nextDouble() * 0.7D;
		double radius = 6.0D + dragon.getRandom().nextDouble() * 6.0D;
		BlockPos center = BlockPos.containing(
				dragon.getX() + Math.cos(angle) * radius,
				dragon.getY(),
				dragon.getZ() + Math.sin(angle) * radius
		);

		for (int y = 6; y >= -10; y--) {
			BlockPos candidate = center.offset(0, y, 0);
			BlockPos ground = candidate.below();
			if (!serverLevel.isInWorldBounds(candidate)) {
				continue;
			}
			if (!serverLevel.getBlockState(ground).blocksMotion()) {
				continue;
			}
			if (!serverLevel.getBlockState(candidate).isAir() || !serverLevel.getBlockState(candidate.above()).isAir()) {
				continue;
			}
			return candidate;
		}

		return null;
	}

	@Unique
	private Player enhancem$findNearestPlayer(ServerLevel serverLevel, Vec3 center) {
		AABB box = new AABB(center, center).inflate(ENHANCEM_SUMMON_RADIUS);
		List<Player> players = serverLevel.getEntitiesOfClass(Player.class, box, Player::isAlive);
		Player nearest = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Player player : players) {
			double distance = player.distanceToSqr(center);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = player;
			}
		}
		return nearest;
	}
}
