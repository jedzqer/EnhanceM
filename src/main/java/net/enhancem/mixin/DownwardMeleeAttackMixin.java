package net.enhancem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class DownwardMeleeAttackMixin {

	@Unique
	private static final double EXTRA_DOWNWARD_ATTACK_DEPTH = 1.0D;
	@Unique
	private static final double EXTRA_DOWNWARD_ATTACK_HEIGHT = 0.6D;
	@Unique
	private static final double EXTRA_DOWNWARD_ATTACK_HORIZONTAL_EXPANSION = 0.9D;
	@Unique
	private static final double FORWARD_BLOCK_SAMPLE_DISTANCE = 0.6D;
	@Unique
	private static final double MIN_DIRECTION_LENGTH_SQR = 1.0E-6D;

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	protected abstract boolean isTimeToAttack();

	@Inject(method = "canPerformAttack", at = @At("RETURN"), cancellable = true)
	private void enhancem$allowDownwardAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			return;
		}

		if (!this.isTimeToAttack() || !target.isAlive()) {
			return;
		}

		if (!this.enhancem$isTargetInDownwardAttackWindow(target)) {
			return;
		}

		if (!this.enhancem$canReachPastLowerFrontBlock(target)) {
			return;
		}

		cir.setReturnValue(true);
	}

	@Unique
	private boolean enhancem$isTargetInDownwardAttackWindow(LivingEntity target) {
		AABB mobBox = this.mob.getBoundingBox();
		AABB targetBox = target.getBoundingBox();
		AABB attackWindow = new AABB(
			mobBox.minX - EXTRA_DOWNWARD_ATTACK_HORIZONTAL_EXPANSION,
			mobBox.minY - EXTRA_DOWNWARD_ATTACK_DEPTH,
			mobBox.minZ - EXTRA_DOWNWARD_ATTACK_HORIZONTAL_EXPANSION,
			mobBox.maxX + EXTRA_DOWNWARD_ATTACK_HORIZONTAL_EXPANSION,
			mobBox.minY + EXTRA_DOWNWARD_ATTACK_HEIGHT,
			mobBox.maxZ + EXTRA_DOWNWARD_ATTACK_HORIZONTAL_EXPANSION
		);
		return attackWindow.intersects(targetBox) && targetBox.minY < mobBox.minY && targetBox.maxY <= mobBox.minY + EXTRA_DOWNWARD_ATTACK_HEIGHT;
	}

	@Unique
	private boolean enhancem$canReachPastLowerFrontBlock(LivingEntity target) {
		Vec3 forward = new Vec3(target.getX() - this.mob.getX(), 0.0D, target.getZ() - this.mob.getZ());
		if (forward.lengthSqr() < MIN_DIRECTION_LENGTH_SQR) {
			return true;
		}

		Vec3 direction = forward.normalize();
		Vec3 samplePoint = new Vec3(
			this.mob.getX() + direction.x * FORWARD_BLOCK_SAMPLE_DISTANCE,
			this.mob.getBoundingBox().minY + 0.25D,
			this.mob.getZ() + direction.z * FORWARD_BLOCK_SAMPLE_DISTANCE
		);
		BlockPos samplePos = BlockPos.containing(samplePoint);
		Level level = this.mob.level();
		BlockState state = level.getBlockState(samplePos);
		return state.isAir() || !state.isCollisionShapeFullBlock(level, samplePos);
	}
}
