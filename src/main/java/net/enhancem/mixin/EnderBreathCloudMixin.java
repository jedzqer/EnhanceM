package net.enhancem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AreaEffectCloud.class)
public class EnderBreathCloudMixin {

	@Unique
	private static final double ENHANCEM_VERTICAL_RANDOM_RANGE = 8.0D;
	@Unique
	private static final int ENHANCEM_ENDERMAN_AGGRO_RADIUS = 24;

	@Inject(method = "tick", at = @At("TAIL"))
	private void enhancem$applyEnderBreathEffects(CallbackInfo ci) {
		AreaEffectCloud cloud = (AreaEffectCloud) (Object) this;
		if (!(cloud.getOwner() instanceof EnderDragon)) {
			return;
		}
		if (!(cloud.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		if (cloud.isRemoved()) {
			return;
		}

		double radius = cloud.getRadius();
		Vec3 center = cloud.position();
		AABB bounds = new AABB(center, center).inflate(radius);
		List<Player> players = serverLevel.getEntitiesOfClass(Player.class, bounds, Player::isAlive);
		for (Player player : players) {
			if (player.position().distanceToSqr(center) > radius * radius) {
				continue;
			}

			Vec3 teleportPos = this.enhancem$findTeleportPosition(serverLevel, player);
			if (teleportPos != null) {
				player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
			}
			this.enhancem$angerNearbyEndermen(serverLevel, player, center);
		}

		this.enhancem$spawnSphereParticles(serverLevel, center, radius);
	}

	@Unique
	private Vec3 enhancem$findTeleportPosition(ServerLevel level, Player player) {
		for (int i = 0; i < 16; i++) {
			double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 32.0D;
			double y = player.getY() + (player.getRandom().nextDouble() - 0.5D) * ENHANCEM_VERTICAL_RANDOM_RANGE;
			double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 32.0D;
			BlockPos pos = BlockPos.containing(x, y, z);
			if (!level.isInWorldBounds(pos)) {
				continue;
			}

			BlockPos groundPos = pos;
			while (groundPos.getY() > level.getMinY() && level.getBlockState(groundPos).isAir()) {
				groundPos = groundPos.below();
			}

			BlockPos standPos = groundPos.above();
			BlockState feetState = level.getBlockState(standPos);
			BlockState headState = level.getBlockState(standPos.above());
			if (!feetState.isAir() || !headState.isAir()) {
				continue;
			}

			return Vec3.atBottomCenterOf(standPos);
		}
		return null;
	}

	@Unique
	private void enhancem$angerNearbyEndermen(ServerLevel level, Player player, Vec3 center) {
		AABB aggroBounds = new AABB(center, center).inflate(ENHANCEM_ENDERMAN_AGGRO_RADIUS);
		List<EnderMan> endermen = level.getEntitiesOfClass(EnderMan.class, aggroBounds, Entity::isAlive);
		for (EnderMan enderMan : endermen) {
			enderMan.setTarget(player);
		}
	}

	@Unique
	private void enhancem$spawnSphereParticles(ServerLevel level, Vec3 center, double radius) {
		AreaEffectCloud cloud = (AreaEffectCloud) (Object) this;
		for (int i = 0; i < 18; i++) {
			Vec3 offset = new Vec3(
					cloud.getRandom().nextDouble() * 2.0D - 1.0D,
					cloud.getRandom().nextDouble() * 2.0D - 1.0D,
					cloud.getRandom().nextDouble() * 2.0D - 1.0D
			);
			if (offset.lengthSqr() > 1.0D || offset.lengthSqr() < 0.01D) {
				continue;
			}

			Vec3 particlePos = center.add(offset.normalize().scale(cloud.getRandom().nextDouble() * radius));
			level.sendParticles(
					cloud.getParticle(),
					particlePos.x,
					particlePos.y,
					particlePos.z,
					1,
					0.0D,
					0.0D,
					0.0D,
					0.0D
			);
		}
	}
}
