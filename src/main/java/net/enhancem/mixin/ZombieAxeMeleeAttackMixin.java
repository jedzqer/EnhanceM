package net.enhancem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.SwingAnimation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public abstract class ZombieAxeMeleeAttackMixin {

	@Shadow
	@Final
	protected PathfinderMob mob;

	@Shadow
	protected abstract boolean canPerformAttack(LivingEntity target);

	@Shadow
	protected abstract void resetAttackCooldown();

	@Unique
	private long enhancem$nextTargetScanTick;

	@Inject(method = {"start", "stop"}, at = @At("TAIL"))
	private void enhancem$resetTargetScan(CallbackInfo ci) {
		this.enhancem$nextTargetScanTick = 0;
	}

	@Inject(method = "checkAndPerformAttack", at = @At("HEAD"), cancellable = true)
	private void enhancem$attackNearestPlayer(LivingEntity target, CallbackInfo ci) {
		if (!(this.mob instanceof Zombie zombie) || zombie.level().isClientSide() || !this.enhancem$isAxeZombie()) {
			return;
		}

		LivingEntity attackTarget = this.enhancem$selectNearestPlayerTarget(zombie, target);
		if (attackTarget != null && attackTarget != zombie.getTarget()) {
			zombie.setTarget(attackTarget);
		}

		if (attackTarget != null && this.canPerformAttack(attackTarget)) {
			this.resetAttackCooldown();
			zombie.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT);
			zombie.doHurtTarget((ServerLevel)zombie.level(), attackTarget);
		}

		ci.cancel();
	}

	@Unique
	private boolean enhancem$isAxeZombie() {
		return this.mob instanceof Zombie && this.mob.getMainHandItem().is(ItemTags.AXES);
	}

	@Unique
	private LivingEntity enhancem$selectNearestPlayerTarget(Zombie zombie, LivingEntity fallbackTarget) {
		long now = zombie.level().getGameTime();
        boolean validFallback = fallbackTarget != null && fallbackTarget.isAlive()
            && (!(fallbackTarget instanceof Player player) || !player.isCreative() && !player.isSpectator());
        if (now < this.enhancem$nextTargetScanTick) return validFallback ? fallbackTarget : null;
        this.enhancem$nextTargetScanTick = now + 10 + Math.floorMod(zombie.getId(), 5);
        Player nearestPlayer = zombie.level().getNearestPlayer(zombie.getX(), zombie.getY(), zombie.getZ(),
            zombie.getAttributeValue(Attributes.FOLLOW_RANGE),
            entity -> entity instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator());
		if (nearestPlayer != null && nearestPlayer.isAlive()) {
			return nearestPlayer;
		}
		return validFallback ? fallbackTarget : null;
	}

}
