package net.enhancem.mixin;

import net.minecraft.core.BlockPos;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public class SpiderWebMixin {

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	private boolean isTimeToAttack() { return false; }

	@Unique
	private static final double COBWEB_CHANCE = 0.25;

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"))
	private void onAttackTarget(LivingEntity target, CallbackInfo ci) {
		if (!this.isTimeToAttack()) return;
		if (!(this.mob instanceof Spider)) return;
		if (!(target instanceof Player player)) return;
		if (Math.random() >= COBWEB_CHANCE) return;

		Level level = player.level();
		BlockPos pos = player.blockPosition();

		if (level.getBlockState(pos).isAir()) {
			level.setBlock(pos, Blocks.COBWEB.defaultBlockState(), 3);
		}
	}
}
