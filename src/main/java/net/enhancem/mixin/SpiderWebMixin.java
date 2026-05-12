package net.enhancem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MeleeAttackGoal.class)
public abstract class SpiderWebMixin {

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Unique
	private static final double COBWEB_CHANCE = 0.5;

	@Redirect(
		method = "checkAndPerformAttack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/entity/PathfinderMob;doHurtTarget(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z"
		)
	)
	private boolean enhancem$spawnCobwebAfterSuccessfulAttack(PathfinderMob mob, ServerLevel level, Entity target) {
		boolean wasHurt = mob.doHurtTarget(level, target);
		if (!wasHurt) return false;
		if (!(mob instanceof Spider)) return true;
		if (!(target instanceof Player player)) return true;
		if (mob.getRandom().nextDouble() >= COBWEB_CHANCE) return true;

		Level playerLevel = player.level();
		BlockPos pos = player.blockPosition();

		if (playerLevel.getBlockState(pos).isAir()) {
			playerLevel.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
		}

		return true;
	}
}
